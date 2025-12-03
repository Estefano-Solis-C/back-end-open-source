package com.codexateam.platform.iot.application.internal.services;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Service for simulating realistic vehicle telemetry data.
 * Uses OpenRouteService API to fetch real road coordinates and simulates
 * a vehicle traveling along that route with realistic speed and fuel consumption.
 */
@Service
public class TelemetrySimulatorService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetrySimulatorService.class);

    // Fixed simulation points in Lima, Peru
    private static final double START_LAT = -12.0464;
    private static final double START_LNG = -77.0428;
    private static final double END_LAT = -12.119;
    private static final double END_LNG = -77.029;

    // Simulation parameters
    private static final double INITIAL_FUEL_LEVEL = 100.0;
    private static final double FUEL_CONSUMPTION_RATE = 0.3;


    // In-memory caches to prevent repeated external API calls
    private final Map<Long, List<double[]>> routeCache = new ConcurrentHashMap<>();
    private final Map<Long, Integer> routeIndexCache = new ConcurrentHashMap<>();
    private final Map<Long, Double> fuelCache = new ConcurrentHashMap<>();

    private final OpenRouteServiceApiClient routeClient;
    private final TelemetryRepository telemetryRepository;

    public TelemetrySimulatorService(
            OpenRouteServiceApiClient routeClient,
            TelemetryRepository telemetryRepository) {
        this.routeClient = routeClient;
        this.telemetryRepository = telemetryRepository;
    }

    /**
     * Non-blocking method to fetch the next telemetry point for a given vehicle.
     * On the first call for a vehicleId, it generates and caches the full route.
     * Subsequent calls return the next cached point using a simple counter.
     * This avoids calling the external API multiple times and prevents timeouts.
     *
     * @param vehicleId vehicle identifier (can represent bookingId)
     * @return Optional<Telemetry> with the next telemetry point; empty if route not available
     */
    public Optional<Telemetry> fetchNextTelemetry(Long vehicleId) {
        // Ensure the route is loaded in cache
        routeCache.computeIfAbsent(vehicleId, id -> {
            List<double[]> coords = loadRouteOrFallback(START_LAT, START_LNG, END_LAT, END_LNG);
            if (coords.isEmpty()) {
                // Store an empty list to avoid repeated attempts
                return new ArrayList<>();
            }
            logger.info("Cached route for vehicle {} with {} points", vehicleId, coords.size());
            // Initialize index and fuel for this vehicle
            routeIndexCache.put(vehicleId, 0);
            fuelCache.put(vehicleId, INITIAL_FUEL_LEVEL);
            return coords;
        });

        List<double[]> route = routeCache.get(vehicleId);
        if (route == null || route.isEmpty()) {
            logger.warn("No route available for vehicle {}", vehicleId);
            return Optional.empty();
        }

        int currentIndex = routeIndexCache.computeIfAbsent(vehicleId, k -> 0);
        if (currentIndex >= route.size()) {
            // Loop to start for continuous simulation
            currentIndex = 0;
        }

        double[] coordinate = route.get(currentIndex);
        double latitude = coordinate[0];
        double longitude = coordinate[1];

        // Speed between 30 and 60 km/h
        double speed = 30.0 + ThreadLocalRandom.current().nextDouble(30.0);

        // Fuel consumption using cached fuel
        double currentFuel = fuelCache.getOrDefault(vehicleId, INITIAL_FUEL_LEVEL);
        currentFuel = Math.max(0, currentFuel - FUEL_CONSUMPTION_RATE);
        fuelCache.put(vehicleId, currentFuel);

        RecordTelemetryCommand command = new RecordTelemetryCommand(
                vehicleId,
                latitude,
                longitude,
                speed,
                currentFuel
        );

        Telemetry telemetry = new Telemetry(command);
        telemetryRepository.save(telemetry);

        // Advance index for next request
        routeIndexCache.put(vehicleId, currentIndex + 1);

        logger.debug("Next telemetry for vehicle {} at index {}: lat={}, lng={}, speed={}, fuel={}",
                vehicleId, currentIndex, latitude, longitude,
                String.format("%.2f", speed), String.format("%.2f", currentFuel));

        return Optional.of(telemetry);
    }

    /**
     * Starts an asynchronous simulation of vehicle telemetry along a real route.
     * The simulation runs in a background thread and doesn't block the caller.
     * Implements a fallback mechanism with hardcoded coordinates if API fails.
     *
     * @param vehicleId The ID of the vehicle to simulate telemetry for
     */
    @Async
    public void startSimulation(Long vehicleId) {
        logger.info("Starting INFINITE telemetry simulation for vehicle ID: {}", vehicleId);

        try {
            double currentFuelLevel = INITIAL_FUEL_LEVEL;
            double currentLat = START_LAT;
            double currentLng = START_LNG;
            int totalPointCount = 0;

            // Infinite navigation loop
            while (true) {
                // Generate random destination within Lima bounds
                double destLat = -12.13 + ThreadLocalRandom.current().nextDouble(0.09); // -12.13 to -12.04
                double destLng = -77.08 + ThreadLocalRandom.current().nextDouble(0.13); // -77.08 to -76.95

                logger.info("Vehicle {} - New route: ({}, {}) -> ({}, {})",
                        vehicleId, currentLat, currentLng, destLat, destLng);

                List<double[]> routeCoordinates;

                // Try to fetch route from OpenRouteService API
                if (routeClient.isConfigured()) {
                    routeCoordinates = routeClient.getRouteCoordinates(currentLat, currentLng, destLat, destLng);
                } else {
                    routeCoordinates = List.of();
                }

                // Use fallback if API fails
                if (routeCoordinates.isEmpty()) {
                    logger.warn("Using fallback route for vehicle {}", vehicleId);
                    routeCoordinates = generateHighDensityFallbackRoute(currentLat, currentLng, destLat, destLng);
                } else {
                    logger.info("Route retrieved with {} points for vehicle {}", routeCoordinates.size(), vehicleId);
                }

                // Apply interpolation for smooth movement
                List<double[]> interpolatedRoute = interpolateRoute(routeCoordinates, 10);

                // Navigate through current route
                for (double[] coordinate : interpolatedRoute) {
                    double latitude = coordinate[0];
                    double longitude = coordinate[1];

                    // Random speed between 30 and 60 km/h
                    double speed = 30.0 + ThreadLocalRandom.current().nextDouble(30.0);

                    // Consume fuel
                    currentFuelLevel -= FUEL_CONSUMPTION_RATE;
                    currentFuelLevel = Math.max(0, currentFuelLevel);

                    // Check if refueling is needed
                    if (currentFuelLevel <= 0) {
                        logger.info("Vehicle {} - Fuel depleted. Refueling...", vehicleId);

                        // Save stopped telemetry with speed=0 and fuel=0 (already integers)
                        RecordTelemetryCommand stopCommand = new RecordTelemetryCommand(
                                vehicleId, latitude, longitude, 0.0, 0.0);
                        telemetryRepository.save(new Telemetry(stopCommand));

                        // Simulate refueling time (5 seconds)
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            logger.warn("Refueling interrupted for vehicle {}", vehicleId);
                            Thread.currentThread().interrupt();
                            return;
                        }

                        // Refuel to 100%
                        currentFuelLevel = INITIAL_FUEL_LEVEL;
                        logger.info("Vehicle {} - Refueling complete. Continuing navigation...", vehicleId);
                    }

                    // Round speed and fuel to integers for clean database storage
                    double roundedSpeed = Math.round(speed);
                    double roundedFuel = Math.round(currentFuelLevel);

                    // Save telemetry with integer values
                    RecordTelemetryCommand command = new RecordTelemetryCommand(
                            vehicleId, latitude, longitude, roundedSpeed, roundedFuel);
                    Telemetry telemetry = new Telemetry(command);
                    telemetryRepository.save(telemetry);

                    totalPointCount++;

                    logger.debug("Vehicle {} - Point {}: lat={}, lng={}, speed={}, fuel={}",
                            vehicleId, totalPointCount, latitude, longitude,
                            String.format("%.0f", roundedSpeed), String.format("%.0f", roundedFuel));

                    // Simulate real-time driving with random delay (1000-2000ms for natural timing)
                    try {
                        long randomDelay = ThreadLocalRandom.current().nextLong(1000, 2001);
                        Thread.sleep(randomDelay);
                    } catch (InterruptedException e) {
                        logger.warn("Simulation interrupted for vehicle {}", vehicleId);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Update current position to last point of route (destination reached)
                currentLat = destLat;
                currentLng = destLng;

                logger.info("Vehicle {} - Destination reached. Total points so far: {}. Generating new route...",
                        vehicleId, totalPointCount);
            }

        } catch (Exception e) {
            logger.error("Error during infinite telemetry simulation for vehicle {}", vehicleId, e);
        }
    }

    /**
     * Starts a simulation with custom start and end points.
     * Implements a fallback mechanism with hardcoded coordinates if API fails.
     *
     * @param vehicleId The ID of the vehicle
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     */
    @Async
    public void startSimulation(Long vehicleId, double startLat, double startLng, double endLat, double endLng) {
        logger.info("Starting INFINITE custom telemetry simulation for vehicle ID: {} from ({}, {}) to ({}, {})",
                vehicleId, startLat, startLng, endLat, endLng);

        try {
            double currentFuelLevel = INITIAL_FUEL_LEVEL;
            double currentLat = startLat;
            double currentLng = startLng;
            int totalPointCount = 0;

            // Infinite navigation loop
            while (true) {
                // Generate random destination within Lima bounds
                double destLat = -12.13 + ThreadLocalRandom.current().nextDouble(0.09);
                double destLng = -77.08 + ThreadLocalRandom.current().nextDouble(0.13);

                logger.info("Vehicle {} - New route: ({}, {}) -> ({}, {})",
                        vehicleId, currentLat, currentLng, destLat, destLng);

                List<double[]> routeCoordinates;

                // Try to fetch route from OpenRouteService API
                if (routeClient.isConfigured()) {
                    routeCoordinates = routeClient.getRouteCoordinates(currentLat, currentLng, destLat, destLng);
                } else {
                    routeCoordinates = List.of();
                }

                // Use fallback if API fails
                if (routeCoordinates.isEmpty()) {
                    logger.warn("Using fallback route for vehicle {}", vehicleId);
                    routeCoordinates = generateHighDensityFallbackRoute(currentLat, currentLng, destLat, destLng);
                } else {
                    logger.info("Route retrieved with {} points for vehicle {}", routeCoordinates.size(), vehicleId);
                }

                // Apply interpolation for smooth movement
                List<double[]> interpolatedRoute = interpolateRoute(routeCoordinates, 10);

                // Navigate through current route
                for (double[] coordinate : interpolatedRoute) {
                    double latitude = coordinate[0];
                    double longitude = coordinate[1];

                    // Random speed between 30 and 60 km/h
                    double speed = 30.0 + ThreadLocalRandom.current().nextDouble(30.0);

                    // Consume fuel
                    currentFuelLevel -= FUEL_CONSUMPTION_RATE;
                    currentFuelLevel = Math.max(0, currentFuelLevel);

                    // Check if refueling is needed
                    if (currentFuelLevel <= 0) {
                        logger.info("Vehicle {} - Fuel depleted. Refueling...", vehicleId);

                        // Save stopped telemetry with speed=0 and fuel=0 (already integers)
                        RecordTelemetryCommand stopCommand = new RecordTelemetryCommand(
                                vehicleId, latitude, longitude, 0.0, 0.0);
                        telemetryRepository.save(new Telemetry(stopCommand));

                        // Simulate refueling time (5 seconds)
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            logger.warn("Refueling interrupted for vehicle {}", vehicleId);
                            Thread.currentThread().interrupt();
                            return;
                        }

                        // Refuel to 100%
                        currentFuelLevel = INITIAL_FUEL_LEVEL;
                        logger.info("Vehicle {} - Refueling complete. Continuing navigation...", vehicleId);
                    }

                    // Round speed and fuel to integers for clean database storage
                    double roundedSpeed = Math.round(speed);
                    double roundedFuel = Math.round(currentFuelLevel);

                    // Save telemetry with integer values
                    RecordTelemetryCommand command = new RecordTelemetryCommand(
                            vehicleId, latitude, longitude, roundedSpeed, roundedFuel);
                    Telemetry telemetry = new Telemetry(command);
                    telemetryRepository.save(telemetry);

                    totalPointCount++;

                    logger.debug("Vehicle {} - Point {}: lat={}, lng={}, speed={}, fuel={}",
                            vehicleId, totalPointCount, latitude, longitude,
                            String.format("%.0f", roundedSpeed), String.format("%.0f", roundedFuel));

                    // Simulate real-time driving with random delay (1000-2000ms for natural timing)
                    try {
                        long randomDelay = ThreadLocalRandom.current().nextLong(1000, 2001);
                        Thread.sleep(randomDelay);
                    } catch (InterruptedException e) {
                        logger.warn("Simulation interrupted for vehicle {}", vehicleId);
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                // Update current position to destination
                currentLat = destLat;
                currentLng = destLng;

                logger.info("Vehicle {} - Destination reached. Total points so far: {}. Generating new route...",
                        vehicleId, totalPointCount);
            }

        } catch (Exception e) {
            logger.error("Error during infinite custom telemetry simulation for vehicle {}", vehicleId, e);
        }
    }

    /**
     * Helper to load route from external API or use local fallback immediately.
     * Never blocks callers: if external API fails or returns empty, returns fallback.
     *
     * Fallback Strategy (High Density Path):
     * - Calculates distance between start and end coordinates
     * - Generates 1 intermediate point per 10 meters (minimum 100 points)
     * - Ensures simulation lasts several minutes instead of teleporting
     */
    private List<double[]> loadRouteOrFallback(double startLat, double startLng, double endLat, double endLng) {
        try {
            List<double[]> routeCoordinates = List.of();
            if (!routeClient.isConfigured()) {
                logger.error("⚠️ OpenRouteService API Key NOT configured. Please set 'openrouteservice.api.key' in application.properties");
                logger.warn("⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa no está configurada");
                return generateHighDensityFallbackRoute(startLat, startLng, endLat, endLng);
            }

            routeCoordinates = routeClient.getRouteCoordinates(startLat, startLng, endLat, endLng);

            if (routeCoordinates == null || routeCoordinates.isEmpty()) {
                logger.error("⚠️ OpenRouteService API returned EMPTY response");
                logger.warn("⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló");
                return generateHighDensityFallbackRoute(startLat, startLng, endLat, endLng);
            }

            logger.info("✓ Route successfully loaded from OpenRouteService API with {} points", routeCoordinates.size());
            return routeCoordinates;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Capture HTTP errors specifically (401, 403, 404, etc.)
            logger.error("⚠️ HTTP ERROR calling OpenRouteService API:");
            logger.error("   Status Code: {} ({})", e.getStatusCode().value(), e.getStatusCode());
            logger.error("   Response Body: {}", e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 401) {
                logger.error("   → ERROR 401 UNAUTHORIZED: Your API Key is INVALID or MISSING");
                logger.error("   → Check 'openrouteservice.api.key' in application.properties");
            } else if (e.getStatusCode().value() == 403) {
                logger.error("   → ERROR 403 FORBIDDEN: Your API Key doesn't have permission or quota exceeded");
            } else if (e.getStatusCode().value() == 404) {
                logger.error("   → ERROR 404 NOT FOUND: Route not found between coordinates");
            }

            logger.warn("⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló");
            return generateHighDensityFallbackRoute(startLat, startLng, endLat, endLng);

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("⚠️ NETWORK ERROR calling OpenRouteService API: {}", e.getMessage());
            logger.error("   → Cannot reach api.openrouteservice.org - check your internet connection");
            logger.warn("⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló");
            return generateHighDensityFallbackRoute(startLat, startLng, endLat, endLng);

        } catch (Exception e) {
            logger.error("⚠️ UNEXPECTED ERROR calling OpenRouteService API", e);
            logger.warn("⚠️ ATENCIÓN: Usando ruta simulada (LÍNEA RECTA) porque la API externa falló");
            return generateHighDensityFallbackRoute(startLat, startLng, endLat, endLng);
        }
    }

    /**
     * Generates a high-density fallback route with linear interpolation.
     * Calculates the distance between start and end, then generates intermediate points
     * to ensure smooth and long-duration simulation (minimum 100 points).
     *
     * This prevents the simulation from finishing instantly when the external API fails.
     *
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return List of densely interpolated coordinate points
     */
    private List<double[]> generateHighDensityFallbackRoute(double startLat, double startLng, double endLat, double endLng) {
        logger.warn("════════════════════════════════════════════════════════════════");
        logger.warn("⚠️  ATENCIÓN: Usando ruta simulada (LÍNEA RECTA)");
        logger.warn("⚠️  Motivo: La API de OpenRouteService falló o no está configurada");
        logger.warn("⚠️  El vehículo NO seguirá las calles reales");
        logger.warn("════════════════════════════════════════════════════════════════");

        // Calculate approximate distance using Haversine formula (in meters)
        double distance = calculateDistance(startLat, startLng, endLat, endLng);

        // Generate 1 point per 10 meters, with a minimum of 100 points
        int numberOfPoints = Math.max(100, (int) (distance / 10.0));

        logger.info("Generating FALLBACK route: distance={} meters, generating {} intermediate points (STRAIGHT LINE)",
                    String.format("%.2f", distance), numberOfPoints);

        List<double[]> fallbackRoute = new ArrayList<>(numberOfPoints + 1);

        // Add start point
        fallbackRoute.add(new double[]{startLat, startLng});

        // Generate intermediate points using linear interpolation
        for (int i = 1; i < numberOfPoints; i++) {
            double ratio = (double) i / numberOfPoints;
            double interpolatedLat = startLat + (endLat - startLat) * ratio;
            double interpolatedLng = startLng + (endLng - startLng) * ratio;
            fallbackRoute.add(new double[]{interpolatedLat, interpolatedLng});
        }

        // Add end point
        fallbackRoute.add(new double[]{endLat, endLng});

        logger.info("High-density fallback route generated with {} total points", fallbackRoute.size());
        return fallbackRoute;
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
     * Interpolates a route by generating intermediate points between each pair of coordinates.
     * This prevents "teleporting" by creating smoother transitions between waypoints.
     *
     * @param originalPoints The original route coordinates from the API or fallback
     * @param stepsPerSegment Number of intermediate points to generate between each pair (default: 10)
     * @return A densified list with interpolated points for smooth simulation
     */
    private List<double[]> interpolateRoute(List<double[]> originalPoints, int stepsPerSegment) {
        if (originalPoints == null || originalPoints.size() < 2) {
            logger.warn("Cannot interpolate route with less than 2 points");
            return originalPoints != null ? originalPoints : List.of();
        }

        List<double[]> interpolatedRoute = new ArrayList<>();

        for (int i = 0; i < originalPoints.size() - 1; i++) {
            double[] start = originalPoints.get(i);
            double[] end = originalPoints.get(i + 1);

            // Add the starting point
            interpolatedRoute.add(start);

            // Generate intermediate points using linear interpolation
            for (int step = 1; step < stepsPerSegment; step++) {
                double ratio = (double) step / stepsPerSegment;
                double interpolatedLat = start[0] + (end[0] - start[0]) * ratio;
                double interpolatedLng = start[1] + (end[1] - start[1]) * ratio;
                interpolatedRoute.add(new double[]{interpolatedLat, interpolatedLng});
            }
        }

        // Add the final destination point
        interpolatedRoute.add(originalPoints.getLast());

        logger.info("Route interpolated: {} original points → {} interpolated points",
                originalPoints.size(), interpolatedRoute.size());

        return interpolatedRoute;
    }
}
