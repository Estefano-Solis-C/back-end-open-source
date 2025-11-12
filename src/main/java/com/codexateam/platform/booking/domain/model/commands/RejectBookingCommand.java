package com.codexateam.platform.booking.domain.model.commands;

/**
 * Command to reject a booking.
 * This command is used when a vehicle owner rejects a booking request.
 */
public record RejectBookingCommand(Long bookingId) {
}

