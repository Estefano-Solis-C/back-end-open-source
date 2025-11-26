package com.codexateam.platform.iot.interfaces.rest.transform;

import com.codexateam.platform.iot.domain.model.commands.RecordTelemetryCommand;
import com.codexateam.platform.iot.interfaces.rest.resources.RecordTelemetryResource;

/**
 * Assembler to convert RecordTelemetryResource DTO to RecordTelemetryCommand.
 */
public class RecordTelemetryCommandFromResourceAssembler {
    /**
     * Converts a RecordTelemetryResource DTO to a RecordTelemetryCommand.
     * @param resource The record telemetry resource DTO
     * @return The RecordTelemetryCommand
     */
    public static RecordTelemetryCommand toCommandFromResource(RecordTelemetryResource resource) {
        return new RecordTelemetryCommand(
                resource.vehicleId(),
                resource.latitude(),
                resource.longitude(),
                resource.speed(),
                resource.fuelLevel()
        );
    }
}
