package com.codexateam.platform.booking.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.interfaces.acl.ListingsContextFacade;
import com.codexateam.platform.listings.interfaces.rest.VehiclesController;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("externalListingsServiceBooking")
public class ExternalListingsServiceImpl implements ExternalListingsService {

    private final ListingsContextFacade listingsContextFacade;
    private final VehiclesController vehiclesController;

    public ExternalListingsServiceImpl(ListingsContextFacade listingsContextFacade, VehiclesController vehiclesController) {
        this.listingsContextFacade = listingsContextFacade;
        this.vehiclesController = vehiclesController;
    }

    @Override
    public Optional<VehicleResource> fetchVehicleById(Long vehicleId) {
        try {
            ResponseEntity<VehicleResource> response = vehiclesController.getVehicleById(vehicleId);
            if (response.getStatusCode().is2xxSuccessful()) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (Exception e) {
            // Logging omitted for brevity
        }
        return Optional.empty();
    }

    @Override
    public Optional<Double> getVehiclePriceById(Long vehicleId) {
        return listingsContextFacade.getVehiclePriceById(vehicleId);
    }
}
