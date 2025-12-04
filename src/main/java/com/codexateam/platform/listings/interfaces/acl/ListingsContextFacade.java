package com.codexateam.platform.listings.interfaces.acl;

import com.codexateam.platform.listings.domain.model.aggregates.Vehicle;
import com.codexateam.platform.listings.domain.model.queries.GetVehicleByIdQuery;
import com.codexateam.platform.listings.domain.services.VehicleQueryService;
import org.springframework.stereotype.Service;

import java.util.Optional;

import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleStatusCommand;
import com.codexateam.platform.listings.domain.services.VehicleCommandService;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import com.codexateam.platform.listings.interfaces.rest.transform.VehicleResourceFromEntityAssembler;

@Service
public class ListingsContextFacade {

    private final VehicleQueryService vehicleQueryService;
    private final VehicleCommandService vehicleCommandService;
    private final VehicleResourceFromEntityAssembler vehicleResourceFromEntityAssembler;

    public ListingsContextFacade(VehicleQueryService vehicleQueryService,
                                VehicleCommandService vehicleCommandService,
                                VehicleResourceFromEntityAssembler vehicleResourceFromEntityAssembler) {
        this.vehicleQueryService = vehicleQueryService;
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleResourceFromEntityAssembler = vehicleResourceFromEntityAssembler;
    }

    /**
     * Checks if a vehicle exists by its ID.
     * Used by the IoT bounded context to validate vehicle existence.
     *
     * @param vehicleId The ID of the vehicle to check.
     * @return true if the vehicle exists, false otherwise.
     */
    @SuppressWarnings("unused")
    public boolean existsVehicleById(Long vehicleId) {
        var query = new GetVehicleByIdQuery(vehicleId);
        return vehicleQueryService.handle(query).isPresent();
    }

    /**
     * Retrieves the daily rental price for a specific vehicle.
     * Used by the Booking bounded context to calculate total booking costs.
     *
     * @param vehicleId The ID of the vehicle.
     * @return An Optional containing the price per day if the vehicle exists.
     */
    public Optional<Double> getVehiclePriceById(Long vehicleId) {
        var query = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(query);
        return vehicle.map(Vehicle::getPricePerDay);
    }

    /**
     * Updates the status of a vehicle.
     * Used by the Booking bounded context to change vehicle availability (e.g., "available", "rented").
     *
     * @param vehicleId The ID of the vehicle to update.
     * @param status The new status value.
     * @return An Optional containing the updated Vehicle aggregate if successful.
     */
    public Optional<Vehicle> updateVehicleStatus(Long vehicleId, String status) {
        var command = new UpdateVehicleStatusCommand(vehicleId, status);
        return vehicleCommandService.handle(command);
    }

    /**
     * Retrieves a VehicleResource by ID for cross-context use (ACL for Booking, IoT, etc.).
     * @param vehicleId The ID of the vehicle.
     * @return Optional with VehicleResource if found, otherwise empty.
     */
    public Optional<VehicleResource> getVehicleById(Long vehicleId) {
        var query = new GetVehicleByIdQuery(vehicleId);
        var vehicleOpt = vehicleQueryService.handle(query);
        return vehicleOpt.map(vehicleResourceFromEntityAssembler::toResourceFromEntity);
    }
}
