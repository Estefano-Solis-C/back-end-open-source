package com.codexateam.platform.iot.application.internal.queryservices;

import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.services.TelemetryQueryService;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of TelemetryQueryService.
 */
@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {

    private final TelemetryRepository telemetryRepository;

    public TelemetryQueryServiceImpl(TelemetryRepository telemetryRepository) {
        this.telemetryRepository = telemetryRepository;
    }

    /**
     * Handles getting telemetry data, sorted by creation date descending
     * to get the most recent data first.
     */
    @Override
    public List<Telemetry> handle(GetTelemetryByVehicleIdQuery query) {
        // Sort by 'createdAt' (from AuditableAbstractAggregateRoot) which acts as the timestamp
        return telemetryRepository.findByVehicleId(
            query.vehicleId(), 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }
}
