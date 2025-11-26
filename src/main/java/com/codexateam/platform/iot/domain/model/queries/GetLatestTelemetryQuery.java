package com.codexateam.platform.iot.domain.model.queries;

/**
 * Query to get the latest telemetry record for a vehicle.
 * @param vehicleId The unique identifier of the vehicle
 */
public record GetLatestTelemetryQuery(Long vehicleId) {
}
