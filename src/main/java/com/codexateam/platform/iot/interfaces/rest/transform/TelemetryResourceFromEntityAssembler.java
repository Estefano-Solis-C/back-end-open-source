package com.codexateam.platform.iot.interfaces.rest.transform;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.interfaces.rest.resources.TelemetryResource;

/**
 * Assembler to convert Telemetry aggregate to TelemetryResource DTO.
 */
public class TelemetryResourceFromEntityAssembler {
    public static TelemetryResource toResourceFromEntity(Telemetry entity) {
        return new TelemetryResource(
                entity.getId(),
                entity.getVehicleId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getSpeed(),
                entity.getFuelLevel(),
                entity.getCreatedAt() // Use 'createdAt' as the 'timestamp'
        );
    }
}
