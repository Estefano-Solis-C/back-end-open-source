package com.codexateam.platform.booking.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;

import java.util.Optional;

/**
 * ACL service interface for accessing the Listings bounded context.
 * Provides methods to fetch vehicle information and update vehicle status
 * without coupling the Booking context to Listings internals.
 */
public interface ExternalListingsService {
    /**
     * Fetches vehicle details by its identifier.
     * @param vehicleId The unique identifier of the vehicle
     * @return An Optional containing the VehicleResource if found, empty otherwise
     */
    Optional<VehicleResource> fetchVehicleById(Long vehicleId);

    /**
     * Retrieves the daily rental price for a vehicle.
     * @param vehicleId The unique identifier of the vehicle
     * @return An Optional containing the price per day if found, empty otherwise
     */
    Optional<Double> getVehiclePriceById(Long vehicleId);

    /**
     * Updates the status of a vehicle in the Listings context.
     * @param vehicleId The unique identifier of the vehicle
     * @param status The new status to set (e.g., "available", "rented")
     */
    void updateVehicleStatus(Long vehicleId, String status);
}
