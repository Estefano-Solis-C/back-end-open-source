package com.codexateam.platform.reviews.interfaces.rest.resources;

import java.util.Date;

/**
 * DTO for returning review data.
 *
 */
public record ReviewResource(
        Long id,
        Long vehicleId,
        Long renterId,
        Integer rating,
        String comment,
        Date createdAt
) {
}
