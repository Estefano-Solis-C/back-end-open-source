package com.codexateam.platform.booking.domain.model.commands;

/**
 * Command to confirm a booking.
 * This command is used when a vehicle owner confirms a booking request.
 */
public record ConfirmBookingCommand(Long bookingId) {
}

