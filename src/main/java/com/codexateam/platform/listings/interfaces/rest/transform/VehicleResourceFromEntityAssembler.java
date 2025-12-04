package com.codexateam.platform.listings.interfaces.rest.transform;

import com.codexateam.platform.listings.domain.model.aggregates.Vehicle;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Assembler to convert Vehicle aggregate to VehicleResource DTO.
 * Now a Spring component that injects the base URL from configuration.
 */
@Component
public class VehicleResourceFromEntityAssembler {

    private static final String IMAGE_ENDPOINT = "/api/v1/vehicles/%d/image";

    private final String baseUrl;

    /**
     * Constructor with dependency injection.
     *
     * @param baseUrl The base URL injected from application properties.
     */
    public VehicleResourceFromEntityAssembler(@Value("${app.storage.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * Converts a Vehicle domain entity to a VehicleResource DTO.
     *
     * @param entity The Vehicle aggregate.
     * @return The VehicleResource DTO.
     */
    public VehicleResource toResourceFromEntity(Vehicle entity) {
        String imageUrl = String.format(baseUrl + IMAGE_ENDPOINT, entity.getId());
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
