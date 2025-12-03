package com.codexateam.platform.iot.domain.exceptions;
import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Exception thrown when a vehicle is not found in the Listings context.
 * Used by IoT context when validating telemetry data.
 */
public class VehicleNotFoundException extends DomainException {
    /**
     * Constructs a new VehicleNotFoundException with the vehicle ID.
     * @param vehicleId The ID of the vehicle that was not found
     */
    public VehicleNotFoundException(Long vehicleId) {
        super("Vehicle with ID " + vehicleId + " not found.");
    }
}


