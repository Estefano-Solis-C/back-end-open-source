package com.codexateam.platform.reviews.domain.services;

import com.codexateam.platform.reviews.domain.model.aggregates.Review;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewsByRenterIdQuery;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewsByVehicleIdQuery;
import com.codexateam.platform.reviews.domain.model.queries.GetReviewByIdQuery;

import java.util.List;

/**
 * Service interface for handling Review queries.
 */
public interface ReviewQueryService {
    /**
     * Handles the query to get reviews by vehicle ID.
     */
    List<Review> handle(GetReviewsByVehicleIdQuery query);
    
    /**
     * Handles the query to get reviews by renter ID.
     */
    List<Review> handle(GetReviewsByRenterIdQuery query);

    /**
     * Handles the query to get a single review by its ID.
     * @param query The query containing the review ID.
     * @return The review if found, or empty Optional.
     */
    java.util.Optional<Review> handle(GetReviewByIdQuery query);
}
