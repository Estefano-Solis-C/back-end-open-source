package com.codexateam.platform.reviews.domain.exceptions;

/**
 * Exception thrown when a review with the specified ID cannot be found.
 */
public class ReviewNotFoundException extends RuntimeException {
    /**
     * Constructs a ReviewNotFoundException with the missing review ID.
     * @param id the review ID not found
     */
    public ReviewNotFoundException(Long id) { super("Review with ID " + id + " not found."); }
}
