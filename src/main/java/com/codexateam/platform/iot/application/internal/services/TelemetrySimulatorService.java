package com.codexateam.platform.iot.application.internal.services;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
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
    private static final double START_LAT = -12.046374;  // Lima Centro
    private static final double START_LNG = -77.042793;
    private static final double END_LAT = -12.050000;    // Callao
    private static final double END_LNG = -77.112500;

    // Simulation parameters
    private static final double BASE_SPEED_KMH = 60.0;
    private static final double SPEED_VARIANCE = 15.0;
    private static final double INITIAL_FUEL_LEVEL = 100.0;
    private static final double FUEL_CONSUMPTION_RATE = 0.3; // % per point
    private static final long DELAY_BETWEEN_POINTS_MS = 5000; // 5 seconds

    private final OpenRouteServiceApiClient routeClient;
    private final TelemetryRepository telemetryRepository;

    public TelemetrySimulatorService(
            OpenRouteServiceApiClient routeClient,
            TelemetryRepository telemetryRepository) {
        this.routeClient = routeClient;
        this.telemetryRepository = telemetryRepository;
    }

    /**
     * Starts an asynchronous simulation of vehicle telemetry along a real route.
     * The simulation runs in a background thread and doesn't block the caller.
     *
     * @param vehicleId The ID of the vehicle to simulate telemetry for
     */
    @Async
    public void startSimulation(Long vehicleId) {
        logger.info("Starting telemetry simulation for vehicle ID: {}", vehicleId);

        // Validate that the route service is configured
        if (!routeClient.isConfigured()) {
            logger.error("OpenRouteService is not configured. Cannot start simulation for vehicle {}", vehicleId);
            return;
        }

        try {
            // Fetch real route coordinates from OpenRouteService
            logger.info("Fetching route from OpenRouteService: ({}, {}) -> ({}, {})",
                    START_LAT, START_LNG, END_LAT, END_LNG);

            List<double[]> routeCoordinates = routeClient.getRouteCoordinates(
                    START_LAT, START_LNG,
                    END_LAT, END_LNG
            );

            if (routeCoordinates.isEmpty()) {
                logger.error("No route coordinates returned from OpenRouteService for vehicle {}", vehicleId);
                return;
            }

            logger.info("Route retrieved successfully with {} points. Starting simulation...",
                    routeCoordinates.size());

            // Initialize simulation state
            double currentFuelLevel = INITIAL_FUEL_LEVEL;
            int pointCount = 0;

            // Iterate through each coordinate point on the route
            for (double[] coordinate : routeCoordinates) {
                double latitude = coordinate[0];
                double longitude = coordinate[1];

                // Calculate realistic speed with some variance
                double speed = BASE_SPEED_KMH + ThreadLocalRandom.current().nextDouble(-SPEED_VARIANCE, SPEED_VARIANCE);
                speed = Math.max(0, speed); // Ensure speed is not negative

                // Simulate fuel consumption
                currentFuelLevel -= FUEL_CONSUMPTION_RATE;
                currentFuelLevel = Math.max(0, currentFuelLevel); // Fuel can't go below 0

                // Create telemetry command
                RecordTelemetryCommand command = new RecordTelemetryCommand(
                        vehicleId,
                        latitude,
                        longitude,
                        speed,
                        currentFuelLevel
                );

                // Create and save telemetry
                Telemetry telemetry = new Telemetry(command);
                telemetryRepository.save(telemetry);

                pointCount++;

                logger.debug("Telemetry point {}/{} saved for vehicle {}: lat={}, lng={}, speed={}, fuel={}",
                        pointCount, routeCoordinates.size(), vehicleId, latitude, longitude,
                        String.format("%.2f", speed), String.format("%.2f", currentFuelLevel));

                // Add delay to simulate real-time driving
                try {
                    Thread.sleep(DELAY_BETWEEN_POINTS_MS);
                } catch (InterruptedException e) {
                    logger.warn("Simulation interrupted for vehicle {}", vehicleId);
                    Thread.currentThread().interrupt();
                    break;
                }

                // Stop simulation if fuel runs out
                if (currentFuelLevel <= 0) {
                    logger.info("Fuel depleted. Stopping simulation for vehicle {}", vehicleId);
                    break;
                }
            }

            logger.info("Telemetry simulation completed for vehicle {}. Total points: {}", vehicleId, pointCount);

        } catch (Exception e) {
            logger.error("Error during telemetry simulation for vehicle {}", vehicleId, e);
        }
    }

    /**
     * Starts a simulation with custom start and end points.
     *
     * @param vehicleId The ID of the vehicle
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     */
    @Async
    public void startSimulation(Long vehicleId, double startLat, double startLng, double endLat, double endLng) {
        logger.info("Starting custom telemetry simulation for vehicle ID: {} from ({}, {}) to ({}, {})",
                vehicleId, startLat, startLng, endLat, endLng);

        if (!routeClient.isConfigured()) {
            logger.error("OpenRouteService is not configured. Cannot start simulation for vehicle {}", vehicleId);
            return;
        }

        try {
            List<double[]> routeCoordinates = routeClient.getRouteCoordinates(
                    startLat, startLng,
                    endLat, endLng
            );

            if (routeCoordinates.isEmpty()) {
                logger.error("No route coordinates returned from OpenRouteService for vehicle {}", vehicleId);
                return;
            }

            logger.info("Custom route retrieved with {} points. Starting simulation...",
                    routeCoordinates.size());

            double currentFuelLevel = INITIAL_FUEL_LEVEL;
            int pointCount = 0;

            for (double[] coordinate : routeCoordinates) {
                double latitude = coordinate[0];
                double longitude = coordinate[1];

                double speed = BASE_SPEED_KMH + ThreadLocalRandom.current().nextDouble(-SPEED_VARIANCE, SPEED_VARIANCE);
                speed = Math.max(0, speed);

                currentFuelLevel -= FUEL_CONSUMPTION_RATE;
                currentFuelLevel = Math.max(0, currentFuelLevel);

                RecordTelemetryCommand command = new RecordTelemetryCommand(
                        vehicleId,
                        latitude,
                        longitude,
                        speed,
                        currentFuelLevel
                );

                Telemetry telemetry = new Telemetry(command);
                telemetryRepository.save(telemetry);

                pointCount++;

                logger.debug("Telemetry point {}/{} saved for vehicle {}", pointCount, routeCoordinates.size(), vehicleId);

                try {
                    Thread.sleep(DELAY_BETWEEN_POINTS_MS);
                } catch (InterruptedException e) {
                    logger.warn("Simulation interrupted for vehicle {}", vehicleId);
                    Thread.currentThread().interrupt();
                    break;
                }

                if (currentFuelLevel <= 0) {
                    logger.info("Fuel depleted. Stopping simulation for vehicle {}", vehicleId);
                    break;
                }
            }

            logger.info("Custom telemetry simulation completed for vehicle {}. Total points: {}", vehicleId, pointCount);

        } catch (Exception e) {
            logger.error("Error during custom telemetry simulation for vehicle {}", vehicleId, e);
        }
    }
}

