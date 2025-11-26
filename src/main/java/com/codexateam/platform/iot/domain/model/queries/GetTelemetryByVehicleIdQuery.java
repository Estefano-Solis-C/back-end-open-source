package com.codexateam.platform.iot.domain.model.queries;

/**
 * Query to find all telemetry data for a specific vehicle.
 * @param vehicleId The unique identifier of the vehicle
 */
public record GetTelemetryByVehicleIdQuery(Long vehicleId) {
}
