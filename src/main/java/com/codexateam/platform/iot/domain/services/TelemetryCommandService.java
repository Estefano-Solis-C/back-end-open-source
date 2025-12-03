package com.codexateam.platform.iot.domain.services;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;

/**
 * Service interface for handling Telemetry commands.
 * Following CQRS pattern for command operations.
 */
public interface TelemetryCommandService {
    /**
     * Handles the RecordTelemetryCommand.
     * Validates vehicle existence and user authorization before recording.
     * @param command The command to record telemetry data
     * @param userId The ID of the authenticated user attempting to record telemetry
     * @return The created Telemetry aggregate
     * @throws com.codexateam.platform.iot.domain.exceptions.VehicleNotFoundException if vehicle doesn't exist
     * @throws com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException if user is not the vehicle owner
     */
    Telemetry handle(RecordTelemetryCommand command, Long userId);
}
