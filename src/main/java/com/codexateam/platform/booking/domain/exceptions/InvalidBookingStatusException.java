package com.codexateam.platform.booking.domain.exceptions;

/**
 * Exception thrown when attempting an operation on a booking with an invalid status.
 * For example, trying to confirm a booking that is not in PENDING status.
 */
public class InvalidBookingStatusException extends RuntimeException {

    private final String currentStatus;
    private final String requiredStatus;

    /**
     * Constructs a new InvalidBookingStatusException.
     *
     * @param bookingId The ID of the booking with invalid status
     * @param currentStatus The current status of the booking
     * @param requiredStatus The required status for the operation
     */
    public InvalidBookingStatusException(Long bookingId, String currentStatus, String requiredStatus) {
        super(String.format("Booking %d has status '%s' but operation requires status '%s'",
                           bookingId, currentStatus, requiredStatus));
        this.currentStatus = currentStatus;
        this.requiredStatus = requiredStatus;
    }

    /**
     * Constructs a new InvalidBookingStatusException for operations requiring multiple statuses.
     *
     * @param bookingId The ID of the booking with invalid status
     * @param currentStatus The current status of the booking
     * @param requiredStatuses The list of acceptable statuses for the operation
     */
    public InvalidBookingStatusException(Long bookingId, String currentStatus, String... requiredStatuses) {
        super(String.format("Booking %d has status '%s' but operation requires one of: %s",
                           bookingId, currentStatus, String.join(", ", requiredStatuses)));
        this.currentStatus = currentStatus;
        this.requiredStatus = String.join(" or ", requiredStatuses);
    }

    public String getCurrentStatus() {
        return currentStatus;
    }

    public String getRequiredStatus() {
        return requiredStatus;
    }
}

