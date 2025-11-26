package com.codexateam.platform.iot.application.internal.queryservices;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.model.queries.GetLatestTelemetryQuery;
import com.codexateam.platform.iot.domain.services.TelemetryQueryService;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of TelemetryQueryService.
 * Handles all telemetry-related queries following CQRS pattern.
 */
@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private final TelemetryRepository telemetryRepository;

    public TelemetryQueryServiceImpl(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    /**
     * Retrieves all telemetry data for a vehicle, sorted by creation date descending.
     * This ensures the most recent data appears first.
     * @param query The query containing the vehicle ID
     * @return List of telemetry records sorted by timestamp (most recent first)
     */
    @Override
    public List<Telemetry> handle(GetTelemetryByVehicleIdQuery query) {
        return telemetryRepository.findByVehicleId(
            query.vehicleId(), 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    /**
     * Retrieves the most recent telemetry record for a vehicle.
     * @param query The query containing the vehicle ID
     * @return An Optional containing the latest telemetry if found, empty otherwise
     */
    @Override
    public Optional<Telemetry> handle(GetLatestTelemetryQuery query) {
        return telemetryRepository.findFirstByVehicleIdOrderByCreatedAtDesc(query.vehicleId());
    }
}
