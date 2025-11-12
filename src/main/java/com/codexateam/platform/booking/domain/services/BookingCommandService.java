package com.codexateam.platform.booking.domain.services;

import com.codexateam.platform.booking.domain.model.aggregates.Booking;
import com.codexateam.platform.booking.domain.model.commands.ConfirmBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.CreateBookingCommand;
import com.codexateam.platform.booking.domain.model.commands.RejectBookingCommand;

import java.util.Optional;

/**
 * Service interface for handling Booking commands.
 */
public interface BookingCommandService {
    /**
     * Handles the CreateBookingCommand.
     * @param command The command to create a booking.
     * @return An Optional containing the created Booking aggregate.
     */
    Optional<Booking> handle(CreateBookingCommand command);

    /**
     * Handles the ConfirmBookingCommand.
     * @param command The command to confirm a booking.
     * @return An Optional containing the confirmed Booking aggregate.
     */
    Optional<Booking> handle(ConfirmBookingCommand command);

    /**
     * Handles the RejectBookingCommand.
     * @param command The command to reject a booking.
     * @return An Optional containing the rejected Booking aggregate.
     */
    Optional<Booking> handle(RejectBookingCommand command);
}


