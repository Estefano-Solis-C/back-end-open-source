package com.codexateam.platform.booking.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Domain exception thrown when a Booking aggregate cannot be found.
 */
public class BookingNotFoundException extends DomainException {
    private final Long bookingId;

    public BookingNotFoundException(Long bookingId) {
        super("Booking with ID " + bookingId + " not found.");
        this.bookingId = bookingId;
    }

    public Long getBookingId() {
        return bookingId;
    }
}

