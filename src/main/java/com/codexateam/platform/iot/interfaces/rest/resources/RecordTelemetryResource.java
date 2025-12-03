package com.codexateam.platform.iot.interfaces.rest.resources;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Resource for recording telemetry data.
 * Includes Jakarta Bean Validation constraints for automatic validation.
 */
public record RecordTelemetryResource(
        @NotNull(message = "Vehicle ID is required")
        Long vehicleId,

        @NotNull(message = "Latitude is required")
        @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
        @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
        Double latitude,

        @NotNull(message = "Longitude is required")
        @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
        @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
        Double longitude,

        @NotNull(message = "Speed is required")
        @DecimalMin(value = "0.0", message = "Speed must be greater than or equal to 0")
        Double speed,

        @NotNull(message = "Fuel level is required")
        @DecimalMin(value = "0.0", message = "Fuel level must be between 0 and 100")
        @DecimalMax(value = "100.0", message = "Fuel level must be between 0 and 100")
        Double fuelLevel
) {
}
