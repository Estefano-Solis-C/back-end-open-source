package com.codexateam.platform.iot.interfaces.rest.resources;

/**
 * DTO for recording a new telemetry data point.
 *
 */
public record RecordTelemetryResource(
        Long vehicleId,
        Double latitude,
        Double longitude,
        Double speed,
        Double fuelLevel
) {
}
