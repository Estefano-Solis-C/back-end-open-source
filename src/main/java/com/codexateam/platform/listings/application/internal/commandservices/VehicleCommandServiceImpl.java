package com.codexateam.platform.listings.application.internal.commandservices;

import com.codexateam.platform.listings.application.internal.outboundservices.acl.ExternalIamService;
import com.codexateam.platform.listings.domain.model.aggregates.Vehicle;
import com.codexateam.platform.listings.domain.model.commands.CreateVehicleCommand;
import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleStatusCommand;
import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleCommand;
import com.codexateam.platform.listings.domain.services.VehicleCommandService;
import com.codexateam.platform.listings.infrastructure.persistence.jpa.repositories.VehicleRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

// imports for delete cascade
import org.springframework.transaction.annotation.Transactional;
import com.codexateam.platform.listings.domain.model.commands.DeleteVehicleCommand;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.reviews.infrastructure.persistence.jpa.repositories.ReviewRepository;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;

@Service
public class VehicleCommandServiceImpl implements VehicleCommandService {

    private final VehicleRepository vehicleRepository;
    private final ExternalIamService externalIamService;

    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;
    private final TelemetryRepository telemetryRepository;

    public VehicleCommandServiceImpl(VehicleRepository vehicleRepository,
                                     ExternalIamService externalIamService,
                                     BookingRepository bookingRepository,
                                     ReviewRepository reviewRepository,
                                     TelemetryRepository telemetryRepository) {
        this.vehicleRepository = vehicleRepository;
        this.externalIamService = externalIamService;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
        this.telemetryRepository = telemetryRepository;
    }

    @Override
    public Optional<Vehicle> handle(CreateVehicleCommand command) {
        // Validate ownerId using IAM ACL service (defense in depth)
        if (!externalIamService.isOwner(command.ownerId())) {
            throw new IllegalArgumentException("Owner no existe o no tiene el rol requerido (ROLE_ARRENDADOR)");
        }

        var vehicle = new Vehicle(command);
        try {
            vehicleRepository.save(vehicle);
            return Optional.of(vehicle);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Handles the UpdateVehicleStatusCommand.
     * Updates the rental status of a vehicle (e.g., from "available" to "rented").
     * Used by the Booking context via ACL when bookings are confirmed or completed.
     *
     * @param command The command containing vehicle ID and new status.
     * @return An Optional containing the updated Vehicle, or empty if not found or update fails.
     */
    @Override
    public Optional<Vehicle> handle(UpdateVehicleStatusCommand command) {
        try {
            return vehicleRepository.findById(command.vehicleId())
                    .map(vehicle -> {
                        vehicle.updateStatus(command.status());
                        return vehicleRepository.save(vehicle);
                    });
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    /**
     * Handles the UpdateVehicleCommand.
     * Updates the vehicle's information (brand, model, year, pricePerDay, imageUrl).
     * @param command The command containing the updated vehicle data.
     * @return An Optional containing the updated Vehicle, or empty if not found.
     */
    @Override
    public Optional<Vehicle> handle(UpdateVehicleCommand command) {
        var vehicleOpt = vehicleRepository.findById(command.vehicleId());

        if (vehicleOpt.isEmpty()) {
            return Optional.empty();
        }

        var vehicleToUpdate = vehicleOpt.get();
        vehicleToUpdate.update(command);
        vehicleRepository.save(vehicleToUpdate);

        return Optional.of(vehicleToUpdate);
    }

    @Override
    @Transactional
    public void handle(DeleteVehicleCommand command) {
        Long vehicleId = command.vehicleId();
        // 1. Borrar dependencias primero
        bookingRepository.deleteByVehicleId(vehicleId);
        reviewRepository.deleteByVehicleId(vehicleId);
        telemetryRepository.deleteByVehicleId(vehicleId);
        // 2. Borrar el vehículo
        vehicleRepository.deleteById(vehicleId);
    }
}
