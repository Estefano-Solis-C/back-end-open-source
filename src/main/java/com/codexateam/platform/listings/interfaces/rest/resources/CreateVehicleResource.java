package com.codexateam.platform.listings.interfaces.rest.resources;

/**
 * DTO for creating a new vehicle listing.
 * The owner ID is automatically extracted from the authenticated user's JWT token.
 *
 * @param brand The vehicle's brand/manufacturer.
 * @param model The vehicle's model name.
 * @param year The vehicle's manufacturing year.
 * @param pricePerDay The daily rental price.
 */
public record CreateVehicleResource(
        String brand,
        String model,
        Integer year,
        Double pricePerDay
) {
}
