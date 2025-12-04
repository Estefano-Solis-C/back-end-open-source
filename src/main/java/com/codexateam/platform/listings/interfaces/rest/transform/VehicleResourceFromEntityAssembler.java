package com.codexateam.platform.listings.interfaces.rest.transform;

import com.codexateam.platform.listings.domain.model.aggregates.Vehicle;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Assembler to convert Vehicle aggregate to VehicleResource DTO.
 * Builds dynamic URLs based on the current request context (scheme/host/port) to avoid hardcoding.
 */
@Component
public class VehicleResourceFromEntityAssembler {

    private static final String IMAGE_ENDPOINT_PATTERN = "/api/v1/vehicles/%d/image";

    /**
     * Converts a Vehicle domain entity to a VehicleResource DTO.
     * Dynamically derives base URL from the current HTTP request context.
     *
     * @param entity The Vehicle aggregate.
     * @return The VehicleResource DTO.
     */
    public VehicleResource toResourceFromEntity(Vehicle entity) {
        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .build()
                .toUriString();
        String imageUrl = baseUrl + String.format(IMAGE_ENDPOINT_PATTERN, entity.getId());

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
