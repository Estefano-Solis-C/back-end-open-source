package com.codexateam.platform.reviews.interfaces.rest.transform;

import com.codexateam.platform.reviews.domain.model.aggregates.Review;
import com.codexateam.platform.reviews.interfaces.rest.resources.ReviewResource;

/**
 * Assembler to convert Review aggregate to ReviewResource DTO.
 */
public class ReviewResourceFromEntityAssembler {
    public static ReviewResource toResourceFromEntity(Review entity) {
        return new ReviewResource(
                entity.getId(),
                entity.getVehicleId(),
                entity.getRenterId(),
                entity.getRating(),
                entity.getComment(),
                entity.getCreatedAt()
        );
    }
}
