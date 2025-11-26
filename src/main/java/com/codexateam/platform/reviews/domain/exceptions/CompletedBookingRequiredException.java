package com.codexateam.platform.reviews.domain.exceptions;

/**
 * Exception thrown when a renter attempts to create a review without a completed booking.
 * Only renters who have completed a booking for a vehicle can leave reviews.
 */
public class CompletedBookingRequiredException extends RuntimeException {

    /**
     * Constructs a new CompletedBookingRequiredException.
     *
     * @param renterId The ID of the renter attempting to create a review
     * @param vehicleId The ID of the vehicle for which the review is being created
     */
    public CompletedBookingRequiredException(Long renterId, Long vehicleId) {
        super(String.format("Renter %d must have a completed booking for vehicle %d before leaving a review",
                           renterId, vehicleId));
    }
}

