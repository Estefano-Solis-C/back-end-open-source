package com.codexateam.platform.listings.domain.model.commands;

/**
 * Command to delete an existing vehicle listing.
 *
 * @param vehicleId The ID of the vehicle to delete.
 */
public record DeleteVehicleCommand(Long vehicleId) {}

