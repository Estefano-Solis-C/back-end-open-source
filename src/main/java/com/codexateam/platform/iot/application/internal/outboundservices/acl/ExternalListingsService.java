package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;

import java.util.Optional;

/**
 * Anti-Corruption Layer (ACL) facade for accessing Listings bounded context from IoT.
 * Provides methods to validate vehicle ownership and fetch vehicle data.
 */
public interface ExternalListingsService {

    /**
     * Fetches a vehicle by its ID from the Listings context.
     * @param vehicleId The ID of the vehicle.
     * @return An Optional containing the VehicleResource if found, empty otherwise.
     */
    Optional<VehicleResource> fetchVehicleById(Long vehicleId);

    /**
     * Validates that a specific user is the owner of a vehicle.
     * @param vehicleId The ID of the vehicle.
     * @param userId The ID of the user to validate.
     * @return true if the user is the owner, false otherwise.
     */
    boolean isVehicleOwner(Long vehicleId, Long userId);
}

