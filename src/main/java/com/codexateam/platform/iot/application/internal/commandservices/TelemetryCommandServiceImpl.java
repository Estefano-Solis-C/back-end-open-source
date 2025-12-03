package com.codexateam.platform.iot.application.internal.commandservices;

import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.iot.domain.exceptions.VehicleNotFoundException;
import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.domain.services.TelemetryCommandService;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of TelemetryCommandService.
 * Handles all telemetry-related commands following CQRS pattern.
 * Includes business validation and authorization logic.
 */
@Service
public class TelemetryCommandServiceImpl implements TelemetryCommandService {


    private final TelemetryRepository telemetryRepository;
    private final ExternalListingsService externalListingsService;

    public TelemetryCommandServiceImpl(
            TelemetryRepository telemetryRepository,
            ExternalListingsService externalListingsService) {
        this.telemetryRepository = telemetryRepository;
        this.externalListingsService = externalListingsService;
    }

    /**
     * Handles the RecordTelemetryCommand.
     * Validates vehicle existence and user authorization before recording telemetry.
     * This method encapsulates all business logic and security checks,
     * keeping the controller layer clean and focused on HTTP concerns.
     *
     * @param command The command containing telemetry data to record
     * @param userId The ID of the authenticated user
     * @return The created Telemetry aggregate
     * @throws VehicleNotFoundException if the vehicle doesn't exist
     * @throws UnauthorizedAccessException if the user is not the vehicle owner
     */
    @Override
    @Transactional
    public Telemetry handle(RecordTelemetryCommand command, Long userId) {
        // Business Rule: Validate that the vehicle exists
        if (externalListingsService.fetchVehicleById(command.vehicleId()).isEmpty()) {
            throw new VehicleNotFoundException(command.vehicleId());
        }

        // Business Rule: Only vehicle owner can record telemetry
        if (!externalListingsService.isVehicleOwner(command.vehicleId(), userId)) {
            throw new UnauthorizedAccessException(
                    "Not authorized to record telemetry for vehicle " + command.vehicleId());
        }

        // Create and persist telemetry aggregate
        var telemetry = new Telemetry(command);
        telemetryRepository.save(telemetry);

        return telemetry;
    }
}
