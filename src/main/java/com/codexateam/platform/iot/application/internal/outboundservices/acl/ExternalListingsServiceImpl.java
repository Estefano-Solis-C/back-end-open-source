package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import com.codexateam.platform.listings.domain.model.queries.GetVehicleByIdQuery;
import com.codexateam.platform.listings.domain.services.VehicleQueryService;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import com.codexateam.platform.listings.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of ExternalListingsService ACL for IoT context.
 * Communicates with the Listings bounded context to fetch vehicle data.
 */
@Service("externalListingsServiceIot")
public class ExternalListingsServiceImpl implements ExternalListingsService {

    private final VehicleQueryService vehicleQueryService;
    private final VehicleResourceFromEntityAssembler vehicleResourceFromEntityAssembler;

    public ExternalListingsServiceImpl(VehicleQueryService vehicleQueryService,
                                      VehicleResourceFromEntityAssembler vehicleResourceFromEntityAssembler) {
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleResourceFromEntityAssembler = vehicleResourceFromEntityAssembler;
    }

    @Override
    public Optional<VehicleResource> fetchVehicleById(Long vehicleId) {
        var query = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(query);
        return vehicle.map(vehicleResourceFromEntityAssembler::toResourceFromEntity);
    }

    @Override
    public boolean isVehicleOwner(Long vehicleId, Long userId) {
        var vehicleResource = fetchVehicleById(vehicleId);
        return vehicleResource.isPresent() && vehicleResource.get().ownerId().equals(userId);
    }
}
