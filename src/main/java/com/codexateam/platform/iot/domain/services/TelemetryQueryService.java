package com.codexateam.platform.iot.domain.services;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.model.queries.GetLatestTelemetryQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling Telemetry queries.
 */
public interface TelemetryQueryService {
    /**
     * Handles the query to get telemetry data by vehicle ID.
     * @param query The query containing the vehicle ID
     * @return List of telemetry records for the vehicle
     */
    List<Telemetry> handle(GetTelemetryByVehicleIdQuery query);

    /**
     * Handles the query to get the latest telemetry for a vehicle.
     * @param query The query containing the vehicle ID
     * @return An Optional containing the latest telemetry if found, empty otherwise
     */
    Optional<Telemetry> handle(GetLatestTelemetryQuery query);
}
