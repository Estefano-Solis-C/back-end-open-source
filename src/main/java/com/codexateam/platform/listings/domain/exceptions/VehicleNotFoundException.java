package com.codexateam.platform.listings.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Exception thrown when a vehicle with the specified ID cannot be found.
 */
public class VehicleNotFoundException extends DomainException {
    /**
     * Constructs a VehicleNotFoundException with the missing vehicle ID.
     * @param id the vehicle ID not found
     */
    public VehicleNotFoundException(Long id) {
        super("Vehicle with ID " + id + " not found.");
    }
}
