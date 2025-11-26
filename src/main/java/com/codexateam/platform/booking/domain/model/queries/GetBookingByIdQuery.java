package com.codexateam.platform.booking.domain.model.queries;

/**
 * Query to retrieve a booking by its unique identifier.
 * @param bookingId The unique identifier of the booking
 */
public record GetBookingByIdQuery(Long bookingId) { }

