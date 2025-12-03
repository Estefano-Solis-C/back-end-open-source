package com.codexateam.platform.listings.interfaces.rest.transform;

import com.codexateam.platform.listings.domain.model.commands.CreateVehicleCommand;
import com.codexateam.platform.listings.interfaces.rest.resources.CreateVehicleResource;

/**
 * Assembler to convert CreateVehicleResource DTO to CreateVehicleCommand.
 */
public class CreateVehicleCommandFromResourceAssembler {
    /**
     * Converts a CreateVehicleResource DTO to a CreateVehicleCommand.
     * The owner ID is injected from the security context (authenticated user).
     *
     * @param resource The input DTO with vehicle data.
     * @param image The image data as byte array.
     * @param ownerId The ID of the authenticated owner.
     * @return The CreateVehicleCommand.
     */
    public static CreateVehicleCommand toCommandFromResource(CreateVehicleResource resource, byte[] image, Long ownerId) {
        return new CreateVehicleCommand(
                resource.brand(),
                resource.model(),
                resource.year(),
                resource.pricePerDay(),
                image,
                ownerId
        );
    }
}
