package com.codexateam.platform.reviews.domain.model.queries;

/**
 * Query to find all reviews written by a specific renter (Arrendatario).
 * @param renterId The ID of the renter whose reviews are requested.
 */
public record GetReviewsByRenterIdQuery(Long renterId) {
}
