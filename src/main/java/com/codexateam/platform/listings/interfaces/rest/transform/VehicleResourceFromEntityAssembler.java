package com.codexateam.platform.listings.interfaces.rest.transform;

import com.codexateam.platform.listings.domain.model.aggregates.Vehicle;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;

/**
 * Assembler to convert Vehicle aggregate to VehicleResource DTO.
 */
public class VehicleResourceFromEntityAssembler {

    private static final String BASE_URL = "http://localhost:8080";
    private static final String IMAGE_ENDPOINT = "/api/v1/vehicles/%d/image";

    /**
     * Converts a Vehicle domain entity to a VehicleResource DTO.
     *
     * @param entity The Vehicle aggregate.
     * @return The VehicleResource DTO.
     */
    public static VehicleResource toResourceFromEntity(Vehicle entity) {
        String imageUrl = String.format(BASE_URL + IMAGE_ENDPOINT, entity.getId());
        return new VehicleResource(
                entity.getId(),
                entity.getBrand(),
                entity.getModel(),
                entity.getYear(),
                entity.getPricePerDay(),
                entity.getStatus(),
                imageUrl,
                entity.getOwnerId(),
                entity.getCreatedAt()
        );
    }
}
