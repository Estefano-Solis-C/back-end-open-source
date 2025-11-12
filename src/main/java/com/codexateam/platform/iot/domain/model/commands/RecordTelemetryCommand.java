package com.codexateam.platform.iot.domain.model.commands;

/**
 * Command to record a new telemetry data point for a vehicle.
 *
 * @param vehicleId The ID of the vehicle.
 * @param latitude The current latitude.
 * @param longitude The current longitude.
 * @param speed The current speed.
 * @param fuelLevel The current fuel level percentage.
 */
public record RecordTelemetryCommand(
        Long vehicleId,
        Double latitude,
        Double longitude,
        Double speed,
        Double fuelLevel
) {
}
