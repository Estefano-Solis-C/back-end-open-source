package com.codexateam.platform.iot.application.internal.outboundservices;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for automatically generating telemetry data for active bookings.
 * Implements a simple state machine per vehicle:
 *  - MOVIENDO: consume next route point, update telemetry with speed>0, consume fuel
 *  - DETENIDO/ESPERA: when route finished, remain stopped for 10 seconds (speed=0, no fuel consumption)
 *  - REINICIO: after wait, plan a new route from current position to a random point in Lima and go back to MOVIENDO
 */
@Service
@EnableScheduling
public class AutomaticTelemetryGeneratorService {

    private static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";

    // Lima bounds
    private static final double LIMA_LAT_MIN = -12.13;
    private static final double LIMA_LAT_MAX = -12.04;
    private static final double LIMA_LNG_MIN = -77.08;
    private static final double LIMA_LNG_MAX = -76.95;

    // Movement
    private static final double MIN_SPEED_KMH = 30.0;
    private static final double MAX_SPEED_KMH = 60.0;

    private static final long STOP_WAIT_MILLIS = 10_000L;

    // Route storage per vehicle
    private final Map<Long, Queue<double[]>> vehicleRoutes = new ConcurrentHashMap<>();

    // Fuel state per vehicle (percentage 0..100)
    private final Map<Long, Double> vehicleFuelLevels = new ConcurrentHashMap<>();

    // State machine per vehicle
    private enum VehicleState { MOVIENDO, DETENIDO }
    private final Map<Long, VehicleState> vehicleStates = new ConcurrentHashMap<>();
    private final Map<Long, Long> vehicleWaitUntil = new ConcurrentHashMap<>();

    private final BookingRepository bookingRepository;
    private final TelemetryRepository telemetryRepository;
    private final OpenRouteServiceApiClient openRouteServiceApiClient;

    // Opcional: mapa de heartbeats por compatibilidad con el controlador
    private final Map<Long, Date> lastHeartbeatMap = new ConcurrentHashMap<>();

    public AutomaticTelemetryGeneratorService(
            BookingRepository bookingRepository,
            TelemetryRepository telemetryRepository,
            OpenRouteServiceApiClient openRouteServiceApiClient) {
        this.bookingRepository = bookingRepository;
        this.telemetryRepository = telemetryRepository;
        this.openRouteServiceApiClient = openRouteServiceApiClient;
    }

    /**
     * Permite al frontend inferir el estado actual del vehículo.
     */
    public String getVehicleState(Long vehicleId) {
        VehicleState state = vehicleStates.getOrDefault(vehicleId, VehicleState.DETENIDO);
        return state == VehicleState.MOVIENDO ? "Moviendose" : "Detenido";
    }

    /**
     * Guarda/actualiza telemetría en una sola fila por vehículo.
     */
    private void saveOrUpdateTelemetry(Long vehicleId, double latitude, double longitude, double speed, double fuelLevel) {
        List<Telemetry> telemetryList = telemetryRepository.findByVehicleId(vehicleId);
        Telemetry telemetry;
        if (!telemetryList.isEmpty()) {
            telemetry = telemetryList.getFirst();
            telemetry.setLatitude(latitude);
            telemetry.setLongitude(longitude);
            telemetry.setSpeed(speed);
            telemetry.setFuelLevel(fuelLevel);
        } else {
            RecordTelemetryCommand command = new RecordTelemetryCommand(vehicleId, latitude, longitude, speed, fuelLevel);
            telemetry = new Telemetry(command);
        }
        telemetryRepository.save(telemetry);
    }

    /**
     * Compatibilidad: notifica que el frontend está monitoreando un vehículo.
     * No afecta la máquina de estados, pero se mantiene para evitar romper el controlador.
     */
    public void notifyActiveMonitoring(Long vehicleId) {
        if (vehicleId == null) return;
        lastHeartbeatMap.put(vehicleId, new Date());
    }

    /**
     * Exponer la ruta planificada actual (si existe) para dibujo en frontend.
     */
    public List<double[]> getPlannedRoute(Long vehicleId) {
        Queue<double[]> queue = vehicleRoutes.get(vehicleId);
        if (queue == null || queue.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(queue);
    }

    private double[] getRandomCoordinateInLima() {
        double lat = LIMA_LAT_MIN + ThreadLocalRandom.current().nextDouble(LIMA_LAT_MAX - LIMA_LAT_MIN);
        double lng = LIMA_LNG_MIN + ThreadLocalRandom.current().nextDouble(LIMA_LNG_MAX - LIMA_LNG_MIN);
        return new double[]{lat, lng};
    }

    private List<double[]> decimateRoute(List<double[]> path) {
        if (path == null || path.isEmpty()) return path;
        List<double[]> result = new ArrayList<>();
        for (int i = 0; i < path.size(); i++) {
            if (i % 10 == 0) result.add(path.get(i));
        }
        double[] last = path.getLast();
        if (result.isEmpty() || result.getLast() != last) {
            result.add(last);
        }
        return result;
    }

    private List<double[]> generateHighDensityFallbackPath(double[] start, double[] end) {
        double startLat = start[0];
        double startLng = start[1];
        double endLat = end[0];
        double endLng = end[1];
        double distance = calculateDistance(startLat, startLng, endLat, endLng);
        int numberOfPoints = Math.max(100, (int) (distance / 10.0));


        List<double[]> fallbackPath = new ArrayList<>(numberOfPoints + 1);
        fallbackPath.add(boundToLima(start));
        for (int i = 1; i < numberOfPoints; i++) {
            double ratio = (double) i / numberOfPoints;
            double interpolatedLat = startLat + (endLat - startLat) * ratio;
            double interpolatedLng = startLng + (endLng - startLng) * ratio;
            interpolatedLat = ensureWithinBounds(interpolatedLat, LIMA_LAT_MIN, LIMA_LAT_MAX);
            interpolatedLng = ensureWithinBounds(interpolatedLng, LIMA_LNG_MIN, LIMA_LNG_MAX);
            fallbackPath.add(new double[]{interpolatedLat, interpolatedLng});
        }
        fallbackPath.add(boundToLima(end));
        return fallbackPath;
    }

    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS_METERS = 6371000.0;
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }

    private double consumeFuel(Long vehicleId) {
        double current = vehicleFuelLevels.getOrDefault(vehicleId, 100.0);
        double decrement = 0.2 + ThreadLocalRandom.current().nextDouble(0.3);
        current = Math.max(0.0, current - decrement);
        vehicleFuelLevels.put(vehicleId, current);
        return current;
    }

    private double randomSpeedKmh() {
        return MIN_SPEED_KMH + ThreadLocalRandom.current().nextDouble(MAX_SPEED_KMH - MIN_SPEED_KMH);
    }

    private double[] boundToLima(double[] coord) {
        return new double[]{
                ensureWithinBounds(coord[0], LIMA_LAT_MIN, LIMA_LAT_MAX),
                ensureWithinBounds(coord[1], LIMA_LNG_MIN, LIMA_LNG_MAX)
        };
    }

    private double ensureWithinBounds(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
