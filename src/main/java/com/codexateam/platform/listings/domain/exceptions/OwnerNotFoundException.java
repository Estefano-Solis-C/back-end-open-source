package com.codexateam.platform.listings.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Exception thrown when the specified owner does not exist or lacks required permissions.
 * An owner must have the ROLE_ARRENDADOR role to list vehicles.
 */
public class OwnerNotFoundException extends DomainException {

    /**
     * Constructs a new OwnerNotFoundException.
     *
     * @param ownerId The ID of the owner that was not found or lacks permissions
     */
    public OwnerNotFoundException(Long ownerId) {
        super(String.format("Owner with ID %d does not exist or does not have the required ROLE_ARRENDADOR role",
                           ownerId));
    }
}

