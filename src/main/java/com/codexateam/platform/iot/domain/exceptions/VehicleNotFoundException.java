package com.codexateam.platform.iot.domain.exceptions;

/**
 * Exception thrown when a vehicle is not found in the Listings context.
 * Used by IoT context when validating telemetry data.
 */
public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle with ID " + vehicleId + " not found.");
    }
}

