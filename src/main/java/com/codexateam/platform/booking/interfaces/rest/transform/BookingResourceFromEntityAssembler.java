package com.codexateam.platform.booking.interfaces.rest.transform;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.interfaces.rest.resources.BookingResource;

/**
 * Assembler to convert Booking aggregate to BookingResource DTO.
 */
public class BookingResourceFromEntityAssembler {
    /**
     * Converts a Booking entity to a BookingResource DTO.
     * @param entity The Booking aggregate to convert
     * @return The BookingResource DTO
     */
    public static BookingResource toResourceFromEntity(Booking entity) {
        return new BookingResource(
                entity.getId(),
                entity.getVehicleId(),
                entity.getRenterId(),
                entity.getOwnerId(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getTotalPrice(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
