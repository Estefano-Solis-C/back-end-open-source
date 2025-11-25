package com.codexateam.platform.iot.application.internal.commandservices;

import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.iot.domain.exceptions.VehicleNotFoundException;
import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.domain.services.TelemetryCommandService;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of TelemetryCommandService.
 */
@Service
public class TelemetryCommandServiceImpl implements TelemetryCommandService {

    private final TelemetryRepository telemetryRepository;
    private final ExternalListingsService externalListingsService;

    public TelemetryCommandServiceImpl(TelemetryRepository telemetryRepository, ExternalListingsService externalListingsService) {
        this.telemetryRepository = telemetryRepository;
        this.externalListingsService = externalListingsService;
    }

    /**
     * Handles the RecordTelemetryCommand.
     * Validates that the vehicle exists before recording telemetry.
     * TODO: Add additional validation to verify that the authenticated principal (device/user)
     * has permission to post data for this vehicle.
     */
    @Override
    public Optional<Telemetry> handle(RecordTelemetryCommand command) {
        // Validate that the vehicle exists
        if (externalListingsService.fetchVehicleById(command.vehicleId()).isEmpty()) {
            throw new VehicleNotFoundException(command.vehicleId());
        }

        var telemetry = new Telemetry(command);
        try {
            telemetryRepository.save(telemetry);
            return Optional.of(telemetry);
        } catch (Exception e) {
            // Log error
            return Optional.empty();
        }
    }
}
