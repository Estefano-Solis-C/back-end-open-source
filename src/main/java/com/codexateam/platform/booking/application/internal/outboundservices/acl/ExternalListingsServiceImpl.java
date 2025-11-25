package com.codexateam.platform.booking.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.interfaces.acl.ListingsContextFacade;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("externalListingsServiceBooking")
public class ExternalListingsServiceImpl implements ExternalListingsService {

    private final ListingsContextFacade listingsContextFacade;

    public ExternalListingsServiceImpl(ListingsContextFacade listingsContextFacade) {
        this.listingsContextFacade = listingsContextFacade;
    }

    @Override
    public Optional<VehicleResource> fetchVehicleById(Long vehicleId) {
        // Delegates to Facade to avoid coupling with REST controllers.
        try {
            return listingsContextFacade.getVehicleById(vehicleId);
        } catch (Exception e) {
            // Logging omitted for brevity
            return Optional.empty();
        }
    }

    @Override
    public Optional<Double> getVehiclePriceById(Long vehicleId) {
        return listingsContextFacade.getVehiclePriceById(vehicleId);
    }

    @Override
    public void updateVehicleStatus(Long vehicleId, String status) {
        listingsContextFacade.updateVehicleStatus(vehicleId, status);
    }
}
