package com.codexateam.platform.booking.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;

import java.util.Optional;

public interface ExternalListingsService {
    Optional<VehicleResource> fetchVehicleById(Long vehicleId);
    Optional<Double> getVehiclePriceById(Long vehicleId);
}
