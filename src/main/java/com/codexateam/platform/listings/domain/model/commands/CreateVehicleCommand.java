package com.codexateam.platform.listings.domain.model.commands;

/**
 * Command to create a new vehicle listing.
 *
 * @param brand The vehicle's brand/manufacturer.
 * @param model The vehicle's model name.
 * @param year The vehicle's manufacturing year.
 * @param pricePerDay The daily rental price.
 * @param image The image data as byte array.
 * @param ownerId The ID of the owner (must have ROLE_ARRENDADOR).
 */
public record CreateVehicleCommand(
        String brand,
        String model,
        Integer year,
        Double pricePerDay,
        byte[] image,
        Long ownerId
) {
}
