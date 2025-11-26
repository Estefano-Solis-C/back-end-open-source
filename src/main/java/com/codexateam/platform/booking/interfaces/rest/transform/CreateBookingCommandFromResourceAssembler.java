package com.codexateam.platform.booking.interfaces.rest.transform;

import com.codexateam.platform.booking.domain.model.commands.CreateBookingCommand;
import com.codexateam.platform.booking.interfaces.rest.resources.CreateBookingResource;

/**
 * Assembler to convert CreateBookingResource DTO to CreateBookingCommand.
 */
public class CreateBookingCommandFromResourceAssembler {
    /**
     * Converts a CreateBookingResource DTO to a CreateBookingCommand.
     * @param resource The input DTO.
     * @param renterId The ID of the authenticated renter (from security context).
     * @param ownerId The ID of the vehicle's owner (fetched via ACL).
     * @return The CreateBookingCommand.
     */
    public static CreateBookingCommand toCommandFromResource(CreateBookingResource resource, Long renterId, Long ownerId) {
        return new CreateBookingCommand(
                resource.vehicleId(),
                renterId,
                ownerId,
                resource.startDate(),
                resource.endDate()
        );
    }
}
