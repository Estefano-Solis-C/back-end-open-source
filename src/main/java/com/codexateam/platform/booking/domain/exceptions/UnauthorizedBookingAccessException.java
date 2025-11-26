package com.codexateam.platform.booking.domain.exceptions;

/**
 * Exception thrown when a user attempts to access or modify a booking they don't own.
 * Only the booking owner (renter) can cancel their own bookings.
 */
public class UnauthorizedBookingAccessException extends RuntimeException {

    private final Long bookingId;
    private final Long userId;

    /**
     * Constructs a new UnauthorizedBookingAccessException.
     *
     * @param bookingId The ID of the booking being accessed
     * @param userId The ID of the user attempting unauthorized access
     */
    public UnauthorizedBookingAccessException(Long bookingId, Long userId) {
        super(String.format("User %d is not authorized to access or modify booking %d", userId, bookingId));
        this.bookingId = bookingId;
        this.userId = userId;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }
}

