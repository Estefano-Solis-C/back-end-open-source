package com.codexateam.platform.iot.interfaces.rest.resources;

import java.util.Date;

/**
 * DTO for returning telemetry data.
 *
 */
public record TelemetryResource(
        Long id,
        Long vehicleId,
        Double latitude,
        Double longitude,
        Double speed,
        Double fuelLevel,
        Date timestamp // Mapped from 'createdAt'
) {
}
