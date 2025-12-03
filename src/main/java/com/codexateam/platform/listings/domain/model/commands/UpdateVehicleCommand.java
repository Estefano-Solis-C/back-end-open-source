package com.codexateam.platform.listings.domain.model.commands;

/**
 * Command to update an existing vehicle listing.
 * Contains the vehicle ID and all fields that can be modified.
 *
 * @param vehicleId The ID of the vehicle to update.
 * @param brand The updated brand of the vehicle.
 * @param model The updated model of the vehicle.
 * @param year The updated year of the vehicle.
 * @param pricePerDay The updated price per day for renting the vehicle.
 * @param image The updated image data as byte array.
 */
public record UpdateVehicleCommand(
        Long vehicleId,
        String brand,
        String model,
        Integer year,
        Double pricePerDay,
        byte[] image
) {
}

