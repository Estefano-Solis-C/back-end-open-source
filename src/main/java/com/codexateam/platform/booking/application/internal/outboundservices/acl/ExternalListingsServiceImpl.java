package com.codexateam.platform.booking.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.interfaces.acl.ListingsContextFacade;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of ExternalListingsService that delegates to the Listings context facade.
 * Acts as an Anti-Corruption Layer (ACL) to prevent coupling between bounded contexts.
 */
@Service("externalListingsServiceBooking")
public class ExternalListingsServiceImpl implements ExternalListingsService {

    private final ListingsContextFacade listingsContextFacade;

    public ExternalListingsServiceImpl(ListingsContextFacade listingsContextFacade) {
        this.listingsContextFacade = listingsContextFacade;
    }

    /**
     * Fetches vehicle details by delegating to the Listings context facade.
     * @param vehicleId The unique identifier of the vehicle
     * @return An Optional containing the VehicleResource if found, empty on error or not found
     */
    @Override
    public Optional<VehicleResource> fetchVehicleById(Long vehicleId) {
        try {
            return listingsContextFacade.getVehicleById(vehicleId);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Retrieves the daily rental price for a vehicle from the Listings context.
     * @param vehicleId The unique identifier of the vehicle
     * @return An Optional containing the price per day if found, empty otherwise
     */
    @Override
    public Optional<Double> getVehiclePriceById(Long vehicleId) {
        return listingsContextFacade.getVehiclePriceById(vehicleId);
    }

    /**
     * Updates the status of a vehicle in the Listings context.
     * @param vehicleId The unique identifier of the vehicle
     * @param status The new status to set
     */
    @Override
    public void updateVehicleStatus(Long vehicleId, String status) {
        listingsContextFacade.updateVehicleStatus(vehicleId, status);
    }
}
