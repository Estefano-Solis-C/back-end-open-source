package com.codexateam.platform.iot.application.internal.outboundservices;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for automatically generating telemetry data for active bookings.
 * This service runs periodically (every 5 seconds) and simulates vehicle movement
 * for all confirmed bookings that are currently active.
 *
 * <p>The service:</p>
 * <ul>
 *   <li>Finds all bookings with status 'CONFIRMED' that are currently active</li>
 *   <li>For each active booking's vehicle, generates new telemetry data</li>
 *   <li>If no previous telemetry exists, creates a starting point in Lima, Peru</li>
 *   <li>If telemetry exists, simulates movement by incrementing coordinates</li>
 *   <li>Ensures all vehicles stay within Lima's geographic bounds</li>
 * </ul>
 */
@Service
@EnableScheduling
public class AutomaticTelemetryGeneratorService {

    private static final Logger logger = LoggerFactory.getLogger(AutomaticTelemetryGeneratorService.class);
    private static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";

    // Lima, Peru geographic bounds (updated as per requirements)
    private static final double LIMA_LAT_MIN = -12.13; // south
    private static final double LIMA_LAT_MAX = -12.04; // north (closer to 0)
    private static final double LIMA_LNG_MIN = -77.08; // west
    private static final double LIMA_LNG_MAX = -76.95; // east

    // Movement parameters
    private static final double MIN_SPEED_KMH = 30.0;
    private static final double MAX_SPEED_KMH = 60.0;
    private static final double INITIAL_FUEL_LEVEL = 100.0;

    // Stateful route storage per vehicle: planned route as a queue of coordinates [lat, lng]
    private final Map<Long, Queue<double[]>> vehicleRoutes = new ConcurrentHashMap<>();

    // Heartbeat map: last time a vehicle is being actively monitored from frontend
    private final Map<Long, Date> lastHeartbeatMap = new ConcurrentHashMap<>();

    // Fuel state per vehicle (percentage 0..100)
    private final Map<Long, Double> vehicleFuelLevels = new ConcurrentHashMap<>();

    // Stand-by cycles remaining per vehicle (each cycle = 5s). When >0, do not plan new routes
    private final Map<Long, Integer> standbyCyclesRemaining = new ConcurrentHashMap<>();

    private final BookingRepository bookingRepository;
    private final TelemetryRepository telemetryRepository;
    private final OpenRouteServiceApiClient openRouteServiceApiClient;

    /**
     * Constructor with dependency injection.
     */
    public AutomaticTelemetryGeneratorService(
            BookingRepository bookingRepository,
            TelemetryRepository telemetryRepository,
            OpenRouteServiceApiClient openRouteServiceApiClient) {
        this.bookingRepository = bookingRepository;
        this.telemetryRepository = telemetryRepository;
        this.openRouteServiceApiClient = openRouteServiceApiClient;
    }

    /**
     * Public heartbeat notification from frontend. Call this when a user is actively watching a vehicle.
     * Keeps the vehicle eligible for processing for the next 15 seconds window.
     */
    public void notifyActiveMonitoring(Long vehicleId) {
        if (vehicleId == null) return;
        lastHeartbeatMap.put(vehicleId, new Date());
    }

    /**
     * Generates telemetry data for all active trips.
     * Runs automatically every 5 seconds.
     * Logic per vehicle:
     *  A) If a planned route exists and has remaining points, consume next point and save telemetry.
     *  B) If no route (or finished), plan a new route: start (last or random) -> random end (in Lima).
     *     Use OpenRouteService; fallback to linear interpolation (20 points) on failure.
     */
    @Scheduled(fixedRate = 5000)
    public void generateTelemetryForActiveTrips() {
        logger.info("========== SCHEDULED TASK STARTED: generateTelemetryForActiveTrips ==========");

        Date currentTimestamp = new Date();
        logger.info("Current timestamp for query: {}", currentTimestamp);

        List<Booking> activeBookings = bookingRepository.findByBookingStatus_StatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                BOOKING_STATUS_CONFIRMED,
                currentTimestamp,
                currentTimestamp);

        logger.info("Active bookings found: {}", activeBookings.size());

        if (activeBookings.isEmpty()) {
            logger.warn("No active confirmed bookings found for current time. Check: 1) Booking Status = '{}', 2) Start Date <= {}, 3) End Date >= {}",
                    BOOKING_STATUS_CONFIRMED, currentTimestamp, currentTimestamp);
            return;
        }

        logger.info("Autonomous nav: processing {} active bookings", activeBookings.size());

        for (Booking booking : activeBookings) {
            Long vehicleId = booking.getVehicleId();
            Long bookingId = booking.getId();
            logger.info("Processing booking ID: {}, Vehicle ID: {}, Status: {}, Start: {}, End: {}",
                    bookingId, vehicleId,
                    booking.getBookingStatus() != null ? booking.getBookingStatus().status() : "NULL",
                    booking.getStartDate(), booking.getEndDate());

            try {
                Date lastBeat = lastHeartbeatMap.get(vehicleId);
                long timeSinceLastBeat = lastBeat != null
                    ? (new Date().getTime() - lastBeat.getTime()) / 1000
                    : Long.MAX_VALUE;
                if (lastBeat == null || timeSinceLastBeat > 15) {
                    logger.trace("Skipping vehicle {} due to no recent heartbeat", vehicleId);
                    continue;
                }

                int remainingStandby = standbyCyclesRemaining.getOrDefault(vehicleId, 0);
                if (remainingStandby > 0) {
                    double fuel = vehicleFuelLevels.getOrDefault(vehicleId, 100.0);
                    if (fuel < 5.0) {
                        vehicleFuelLevels.put(vehicleId, 100.0);
                        logger.debug("Vehicle {} refueled to 100%% during STAND_BY", vehicleId);
                    }
                    standbyCyclesRemaining.put(vehicleId, remainingStandby - 1);
                    logger.debug("Vehicle {} in STAND_BY ({} cycles remaining)", vehicleId, remainingStandby - 1);
                    continue;
                }

                Telemetry upsertTarget = telemetryRepository
                        .findFirstByVehicleIdOrderByCreatedAtDesc(vehicleId)
                        .orElse(null);

                Queue<double[]> routeQueue = vehicleRoutes.get(vehicleId);
                if (routeQueue != null && !routeQueue.isEmpty()) {
                    double[] next = routeQueue.poll();
                    if (next != null && next.length >= 2) {
                        double speed = randomSpeedKmh();
                        double fuel = consumeFuel(vehicleId);
                        if (upsertTarget == null) {
                            RecordTelemetryCommand command = new RecordTelemetryCommand(vehicleId, next[0], next[1], speed, fuel);
                            upsertTarget = new Telemetry(command);
                        } else {
                            upsertTarget.updateTelemetryData(next[0], next[1], speed, fuel, new Date());
                        }
                        telemetryRepository.save(upsertTarget);
                        logger.debug("Vehicle {} UPSERT telemetry lat={}, lng={} speed={} fuel={} remainingRoute={}",
                                vehicleId,
                                String.format(Locale.US, "%.6f", next[0]),
                                String.format(Locale.US, "%.6f", next[1]),
                                String.format(Locale.US, "%.1f", speed),
                                String.format(Locale.US, "%.1f", fuel),
                                routeQueue.size());
                        if (routeQueue.isEmpty()) {
                            standbyCyclesRemaining.put(vehicleId, 2);
                            double currentFuel = vehicleFuelLevels.getOrDefault(vehicleId, 100.0);
                            if (currentFuel < 5.0) {
                                vehicleFuelLevels.put(vehicleId, 100.0);
                                logger.debug("Vehicle {} refueled to 100%% upon entering STAND_BY", vehicleId);
                            }
                            logger.info("Vehicle {} reached destination. Entering STAND_BY for 10 seconds", vehicleId);
                        }
                        continue;
                    }
                }

                Optional<Telemetry> lastTelemetryOpt = Optional.ofNullable(upsertTarget);
                double[] startPoint = lastTelemetryOpt
                        .map(t -> new double[]{t.getLatitude(), t.getLongitude()})
                        .orElseGet(this::getRandomCoordinateInLima);
                double[] endPoint = getRandomCoordinateInLima();

                List<double[]> plannedPath = openRouteServiceApiClient.getRouteCoordinates(
                        startPoint[0], startPoint[1], endPoint[0], endPoint[1]
                );
                if (plannedPath.isEmpty()) {
                    logger.warn("Route API failed/empty for vehicle {}. Using high-density fallback path.", vehicleId);
                    plannedPath = generateHighDensityFallbackPath(startPoint, endPoint);
                }
                plannedPath = decimateRoute(plannedPath);

                Queue<double[]> newQueue = new ArrayDeque<>(plannedPath.size());
                for (double[] p : plannedPath) {
                    if (p != null && p.length >= 2) newQueue.add(boundToLima(p));
                }
                vehicleRoutes.put(vehicleId, newQueue);

                logger.info("Planned new route for vehicle {} with {} points (start: {}, {} -> end: {}, {})",
                        vehicleId, newQueue.size(),
                        String.format(Locale.US, "%.5f", startPoint[0]), String.format(Locale.US, "%.5f", startPoint[1]),
                        String.format(Locale.US, "%.5f", endPoint[0]), String.format(Locale.US, "%.5f", endPoint[1]));

                double[] first = newQueue.poll();
                if (first != null) {
                    double speed = randomSpeedKmh();
                    double fuel = consumeFuel(vehicleId);
                    if (upsertTarget == null) {
                        RecordTelemetryCommand command = new RecordTelemetryCommand(vehicleId, first[0], first[1], speed, fuel);
                        upsertTarget = new Telemetry(command);
                    } else {
                        upsertTarget.updateTelemetryData(first[0], first[1], speed, fuel, new Date());
                    }
                    telemetryRepository.save(upsertTarget);
                    logger.debug("Vehicle {} started route UPSERT lat={}, lng={} speed={} fuel={} remainingRoute={}",
                            vehicleId,
                            String.format(Locale.US, "%.6f", first[0]),
                            String.format(Locale.US, "%.6f", first[1]),
                            String.format(Locale.US, "%.1f", speed),
                            String.format(Locale.US, "%.1f", fuel),
                            newQueue.size());
                }

            } catch (Exception e) {
                logger.error("Error in autonomous navigation for vehicle {}", vehicleId, e);
            }
        }

        logger.info("Autonomous nav: cycle completed for {} active bookings", activeBookings.size());
    }

    /**
     * Expose the currently planned route for a vehicle as a list (for frontend drawing).
     */
    public List<double[]> getPlannedRoute(Long vehicleId) {
        Queue<double[]> queue = vehicleRoutes.get(vehicleId);
        if (queue == null || queue.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(queue);
    }

    /**
     * Save a telemetry point for a vehicle with provided speed and fuel.
     * Updates existing record if found, creates new one otherwise.
     * This prevents table bloat by maintaining only one row per vehicle.
     */
    private void saveTelemetryPoint(Long vehicleId, double latitude, double longitude, double speed, double fuelLevel) {
        List<Telemetry> telemetryList = telemetryRepository.findByVehicleId(vehicleId);
        Telemetry telemetry;
        if (!telemetryList.isEmpty()) {
            telemetry = telemetryList.getFirst();
            telemetry.setLatitude(latitude);
            telemetry.setLongitude(longitude);
            telemetry.setSpeed(speed);
            telemetry.setFuelLevel(fuelLevel);
        } else {
            RecordTelemetryCommand command = new RecordTelemetryCommand(
                    vehicleId,
                    latitude,
                    longitude,
                    speed,
                    fuelLevel
            );
            telemetry = new Telemetry(command);
        }
        telemetryRepository.save(telemetry);
    }

    /**
     * Returns a random coordinate [lat, lng] within Lima bounds.
     */
    private double[] getRandomCoordinateInLima() {
        double lat = LIMA_LAT_MIN + ThreadLocalRandom.current().nextDouble(LIMA_LAT_MAX - LIMA_LAT_MIN);
        double lng = LIMA_LNG_MIN + ThreadLocalRandom.current().nextDouble(LIMA_LNG_MAX - LIMA_LNG_MIN);
        return new double[]{lat, lng};
    }

    /**
     * Decimates a route by keeping 1 point every 'factor' points, always keeping the last point.
     */
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

    /**
     * Generates a linearly interpolated path between start and end, inclusive of end,
     * with the specified number of intermediate points.
     * Ensures all points remain within Lima bounds.
     */
    private List<double[]> generateInterpolatedPath(double[] start, double[] end, int points) {
        List<double[]> path = new ArrayList<>(points);
        double startLat = start[0];
        double startLng = start[1];
        double endLat = end[0];
        double endLng = end[1];
        for (int i = 1; i <= points; i++) {
            double t = (double) i / points; // 0..1
            double lat = startLat + (endLat - startLat) * t;
            double lng = startLng + (endLng - startLng) * t;
            lat = ensureWithinBounds(lat, LIMA_LAT_MIN, LIMA_LAT_MAX);
            lng = ensureWithinBounds(lng, LIMA_LNG_MIN, LIMA_LNG_MAX);
            path.add(new double[]{lat, lng});
        }
        return path;
    }

    /**
     * Generates a high-density fallback path with linear interpolation.
     * Calculates the distance between start and end, then generates intermediate points
     * to ensure smooth and long-duration simulation (minimum 100 points).
     *
     * This prevents the simulation from finishing instantly when the external API fails.
     * Strategy: 1 point per 10 meters of distance, minimum 100 points.
     *
     * @param start Starting coordinate [lat, lng]
     * @param end Ending coordinate [lat, lng]
     * @return List of densely interpolated coordinate points bounded to Lima
     */
    private List<double[]> generateHighDensityFallbackPath(double[] start, double[] end) {
        double startLat = start[0];
        double startLng = start[1];
        double endLat = end[0];
        double endLng = end[1];

        // Calculate approximate distance using Haversine formula (in meters)
        double distance = calculateDistance(startLat, startLng, endLat, endLng);

        // Generate 1 point per 10 meters, with a minimum of 100 points
        int numberOfPoints = Math.max(100, (int) (distance / 10.0));

        logger.info("Generating high-density fallback path: distance={} meters, {} points",
                    String.format(Locale.US, "%.2f", distance), numberOfPoints);

        List<double[]> fallbackPath = new ArrayList<>(numberOfPoints + 1);

        // Add start point
        fallbackPath.add(boundToLima(start));

        // Generate intermediate points using linear interpolation
        for (int i = 1; i < numberOfPoints; i++) {
            double ratio = (double) i / numberOfPoints;
            double interpolatedLat = startLat + (endLat - startLat) * ratio;
            double interpolatedLng = startLng + (endLng - startLng) * ratio;
            interpolatedLat = ensureWithinBounds(interpolatedLat, LIMA_LAT_MIN, LIMA_LAT_MAX);
            interpolatedLng = ensureWithinBounds(interpolatedLng, LIMA_LNG_MIN, LIMA_LNG_MAX);
            fallbackPath.add(new double[]{interpolatedLat, interpolatedLng});
        }

        // Add end point
        fallbackPath.add(boundToLima(end));

        logger.info("High-density fallback path generated with {} total points", fallbackPath.size());
        return fallbackPath;
    }

    /**
     * Calculates the distance between two coordinates using the Haversine formula.
     * Returns the distance in meters.
     *
     * @param lat1 Latitude of first point
     * @param lng1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lng2 Longitude of second point
     * @return Distance in meters
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS_METERS = 6371000.0; // Earth's radius in meters

        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);

        // Haversine formula
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Consume fuel between 0.2% and 0.5% per movement, clamped to [0,100].
     */
    private double consumeFuel(Long vehicleId) {
        double current = vehicleFuelLevels.getOrDefault(vehicleId, 100.0);
        double decrement = 0.2 + ThreadLocalRandom.current().nextDouble(0.3); // 0.2..0.5
        current = Math.max(0.0, current - decrement);
        vehicleFuelLevels.put(vehicleId, current);
        return current;
    }

    /**
     * Random speed between 30 and 60 km/h.
     */
    private double randomSpeedKmh() {
        return MIN_SPEED_KMH + ThreadLocalRandom.current().nextDouble(MAX_SPEED_KMH - MIN_SPEED_KMH);
    }

    /**
     * Bounds a coordinate to Lima limits.
     */
    private double[] boundToLima(double[] coord) {
        return new double[]{
                ensureWithinBounds(coord[0], LIMA_LAT_MIN, LIMA_LAT_MAX),
                ensureWithinBounds(coord[1], LIMA_LNG_MIN, LIMA_LNG_MAX)
        };
    }

    /**
     * Ensures a coordinate value stays within specified bounds.
     */
    private double ensureWithinBounds(double value, double min, double max) {
        return Math.max(min, Math.min(value, max));
    }
}
