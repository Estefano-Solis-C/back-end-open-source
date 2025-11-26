package com.codexateam.platform.reviews.domain.exceptions;

/**
 * Exception thrown when attempting to create a duplicate review.
 * A renter can only submit one review per vehicle.
 */
public class ReviewAlreadyExistsException extends RuntimeException {

    /**
     * Constructs a new ReviewAlreadyExistsException.
     *
     * @param renterId The ID of the renter attempting to create a duplicate review
     * @param vehicleId The ID of the vehicle that already has a review from this renter
     */
    public ReviewAlreadyExistsException(Long renterId, Long vehicleId) {
        super(String.format("Renter %d has already submitted a review for vehicle %d", renterId, vehicleId));
    }
}

