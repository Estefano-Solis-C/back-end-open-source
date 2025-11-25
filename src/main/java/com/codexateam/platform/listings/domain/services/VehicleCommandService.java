package com.codexateam.platform.listings.domain.services;

import com.codexateam.platform.listings.domain.model.aggregates.Vehicle;
import com.codexateam.platform.listings.domain.model.commands.CreateVehicleCommand;
import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleStatusCommand;
import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleCommand;
import java.util.Optional;
import com.codexateam.platform.listings.domain.model.commands.DeleteVehicleCommand;

/**
 * Service interface for handling Vehicle commands.
 */
public interface VehicleCommandService {
    /**
     * Handles the CreateVehicleCommand.
     * Creates a new vehicle listing with the provided data.
     *
     * @param command The command containing vehicle data and owner ID.
     * @return An Optional containing the created Vehicle aggregate.
     */
    Optional<Vehicle> handle(CreateVehicleCommand command);

    /**
     * Handles the UpdateVehicleStatusCommand.
     * Updates the rental status of a vehicle (e.g., "available", "rented").
     *
     * @param command The command containing vehicle ID and new status.
     * @return An Optional containing the updated Vehicle aggregate.
     */
    Optional<Vehicle> handle(UpdateVehicleStatusCommand command);

    /**
     * Handles the UpdateVehicleCommand.
     * Updates the vehicle's information (brand, model, year, price, image).
     *
     * @param command The command containing vehicle ID and updated data.
     * @return An Optional containing the updated Vehicle aggregate.
     */
    Optional<Vehicle> handle(UpdateVehicleCommand command);

    /**
     * Handles the DeleteVehicleCommand.
     * Deletes a vehicle listing.
     *
     * @param command The command containing the vehicle ID to be deleted.
     */
    void handle(DeleteVehicleCommand command);
}
