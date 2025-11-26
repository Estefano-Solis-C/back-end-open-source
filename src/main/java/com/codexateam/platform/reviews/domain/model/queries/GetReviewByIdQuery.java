package com.codexateam.platform.reviews.domain.model.queries;

/**
 * Query to retrieve a single review by its unique identifier.
 * @param reviewId The ID of the review to retrieve.
 */
public record GetReviewByIdQuery(Long reviewId) {
}

