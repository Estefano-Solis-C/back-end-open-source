package com.codexateam.platform.reviews.domain.model.queries;

/**
 * Query to find all reviews for a specific vehicle.
 * @param vehicleId The ID of the vehicle whose reviews are requested.
 */
public record GetReviewsByVehicleIdQuery(Long vehicleId) {
}
