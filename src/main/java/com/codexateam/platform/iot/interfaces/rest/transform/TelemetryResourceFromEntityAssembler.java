package com.codexateam.platform.iot.interfaces.rest.transform;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.interfaces.rest.resources.TelemetryResource;

import java.util.Collections;
import java.util.List;

/**
 * Assembler to convert Telemetry aggregate to TelemetryResource DTO.
 * Declared as public to be accessible from controllers and services.
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
                entity.getCreatedAt(),
                Collections.emptyList(),
                null
        );
    }

    /**
     * Converts a Telemetry entity to a TelemetryResource DTO with planned route information.
     * @param entity The Telemetry entity to convert
     * @param plannedRoute The list of coordinate pairs representing the planned route
     * @return The TelemetryResource DTO with route information
     */
    public static TelemetryResource toResourceFromEntity(Telemetry entity, List<List<Double>> plannedRoute) {
        return new TelemetryResource(
                entity.getId(),
                entity.getVehicleId(),
                entity.getLatitude(),
                entity.getLongitude(),
                entity.getSpeed(),
                entity.getFuelLevel(),
                entity.getCreatedAt(),
                plannedRoute == null ? Collections.emptyList() : plannedRoute,
                null
        );
    }
}
