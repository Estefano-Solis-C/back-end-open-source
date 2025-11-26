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
    private static final double START_LAT = -12.0464;  // Lima Centro
    private static final double START_LNG = -77.0428;
    private static final double END_LAT = -12.119;    // Miraflores area
    private static final double END_LNG = -77.029;

    // Simulation parameters
    private static final double INITIAL_FUEL_LEVEL = 100.0;
    private static final double FUEL_CONSUMPTION_RATE = 0.3; // % per point

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
     * Implements a fallback mechanism with hardcoded coordinates if API fails.
     *
     * @param vehicleId The ID of the vehicle to simulate telemetry for
     */
    @Async
    public void startSimulation(Long vehicleId) {
        logger.info("Starting telemetry simulation for vehicle ID: {}", vehicleId);

        try {
            List<double[]> routeCoordinates;

            // Try to fetch route from OpenRouteService API
            if (routeClient.isConfigured()) {
                logger.info("Fetching route from OpenRouteService: ({}, {}) -> ({}, {})",
                        START_LAT, START_LNG, END_LAT, END_LNG);

                routeCoordinates = routeClient.getRouteCoordinates(
                        START_LAT, START_LNG,
                        END_LAT, END_LNG
                );
            } else {
                routeCoordinates = List.of(); // Empty list to trigger fallback
            }

            // Check if API returned empty coordinates - use fallback
            if (routeCoordinates.isEmpty()) {
                logger.warn("Using fallback route due to API failure");

                // Hardcoded fallback route in Lima, Peru (5 coordinates)
                routeCoordinates = List.of(
                        new double[]{-12.0464, -77.0428},   // Lima Centro (start)
                        new double[]{-12.0700, -77.0380},   // Point 2
                        new double[]{-12.0900, -77.0340},   // Point 3 (midway)
                        new double[]{-12.1100, -77.0300},   // Point 4
                        new double[]{-12.1190, -77.0290}    // Miraflores (end)
                );
            } else {
                logger.info("Route retrieved successfully with {} points. Starting simulation...",
                        routeCoordinates.size());
            }

            // Initialize simulation state
            double currentFuelLevel = INITIAL_FUEL_LEVEL;
            int pointCount = 0;

            // Iterate through each coordinate point on the route
            for (double[] coordinate : routeCoordinates) {
                double latitude = coordinate[0];
                double longitude = coordinate[1];

                // Set speed to random value between 30 and 60 km/h for moving effect
                double speed = 30.0 + ThreadLocalRandom.current().nextDouble(30.0);

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

                // Add delay to simulate real-time driving (5 seconds)
                try {
                    Thread.sleep(5000);
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
        logger.info("Starting custom telemetry simulation for vehicle ID: {} from ({}, {}) to ({}, {})",
                vehicleId, startLat, startLng, endLat, endLng);

        try {
            List<double[]> routeCoordinates;

            // Try to fetch route from OpenRouteService API
            if (routeClient.isConfigured()) {
                routeCoordinates = routeClient.getRouteCoordinates(
                        startLat, startLng,
                        endLat, endLng
                );
            } else {
                routeCoordinates = List.of(); // Empty list to trigger fallback
            }

            // Check if API returned empty coordinates - use fallback
            if (routeCoordinates.isEmpty()) {
                logger.warn("Using fallback route due to API failure");

                // Hardcoded fallback route in Lima, Peru (5 coordinates)
                routeCoordinates = List.of(
                        new double[]{-12.0464, -77.0428},   // Lima Centro (start)
                        new double[]{-12.0700, -77.0380},   // Point 2
                        new double[]{-12.0900, -77.0340},   // Point 3 (midway)
                        new double[]{-12.1100, -77.0300},   // Point 4
                        new double[]{-12.1190, -77.0290}    // Miraflores (end)
                );
            } else {
                logger.info("Custom route retrieved with {} points. Starting simulation...",
                        routeCoordinates.size());
            }

            double currentFuelLevel = INITIAL_FUEL_LEVEL;
            int pointCount = 0;

            for (double[] coordinate : routeCoordinates) {
                double latitude = coordinate[0];
                double longitude = coordinate[1];

                // Set speed to random value between 30 and 60 km/h for moving effect
                double speed = 30.0 + ThreadLocalRandom.current().nextDouble(30.0);

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

                // Add delay to simulate real-time driving (5 seconds)
                try {
                    Thread.sleep(5000);
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

