package com.codexateam.platform.iot.domain.services;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.model.queries.GetLatestTelemetryQuery;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for handling Telemetry queries.
 * Following CQRS pattern for query operations.
 */
public interface TelemetryQueryService {
    /**
     * Handles the query to get telemetry data by vehicle ID.
     * Validates user authorization before returning data.
     * @param query The query containing the vehicle ID
     * @param userId The ID of the authenticated user requesting the data
     * @return List of telemetry records for the vehicle
     * @throws com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException if user lacks access
     */
    List<Telemetry> handle(GetTelemetryByVehicleIdQuery query, Long userId);

    /**
     * Handles the query to get the latest telemetry for a vehicle.
     * Validates user authorization before returning data.
     * @param query The query containing the vehicle ID
     * @param userId The ID of the authenticated user requesting the data
     * @return An Optional containing the latest telemetry if found, empty otherwise
     * @throws com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException if user lacks access
     */
    Optional<Telemetry> handle(GetLatestTelemetryQuery query, Long userId);
}
