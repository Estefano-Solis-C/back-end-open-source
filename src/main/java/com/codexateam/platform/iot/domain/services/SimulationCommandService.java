package com.codexateam.platform.iot.domain.services;

/**
 * Service interface for handling simulation commands.
 * Following CQRS pattern for command operations.
 */
public interface SimulationCommandService {

    /**
     * Starts a telemetry simulation for a vehicle.
     * Validates that the user is the vehicle owner before starting simulation.
     *
     * @param vehicleId The ID of the vehicle to simulate
     * @param userId The ID of the user requesting the simulation
     * @throws com.codexateam.platform.iot.domain.exceptions.VehicleNotFoundException if vehicle doesn't exist
     * @throws com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException if user is not the owner
     */
    void startSimulation(Long vehicleId, Long userId);

    /**
     * Starts a custom telemetry simulation for a vehicle with specified coordinates.
     * Validates that the user is the vehicle owner before starting simulation.
     *
     * @param vehicleId The ID of the vehicle to simulate
     * @param userId The ID of the user requesting the simulation
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @throws com.codexateam.platform.iot.domain.exceptions.VehicleNotFoundException if vehicle doesn't exist
     * @throws com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException if user is not the owner
     */
    void startCustomSimulation(Long vehicleId, Long userId, double startLat, double startLng, double endLat, double endLng);
}

