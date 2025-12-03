package com.codexateam.platform.iot.application.internal.queryservices;

import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalBookingService;
import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.iot.domain.model.aggregates.Telemetry;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.model.queries.GetLatestTelemetryQuery;
import com.codexateam.platform.iot.domain.services.TelemetryQueryService;
import com.codexateam.platform.iot.infrastructure.persistence.jpa.repositories.TelemetryRepository;
import com.codexateam.platform.shared.domain.exceptions.UnauthorizedAccessException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementation of TelemetryQueryService.
 * Handles all telemetry-related queries following CQRS pattern.
 * Includes authorization logic to ensure users can only access telemetry they're authorized to view.
 */
@Service
public class TelemetryQueryServiceImpl implements TelemetryQueryService {


    private final TelemetryRepository telemetryRepository;
    private final ExternalListingsService externalListingsService;
    private final ExternalBookingService externalBookingService;

    public TelemetryQueryServiceImpl(
            TelemetryRepository telemetryRepository,
            ExternalListingsService externalListingsService,
            ExternalBookingService externalBookingService) {
        this.telemetryRepository = telemetryRepository;
        this.externalListingsService = externalListingsService;
        this.externalBookingService = externalBookingService;
    }

    /**
     * Validates that the user has permission to view telemetry for the vehicle.
     * Business Rule: User must be either the vehicle owner OR have an active booking.
     *
     * @param vehicleId The vehicle ID to check
     * @param userId The user ID requesting access
     * @throws UnauthorizedAccessException if user lacks permission
     */
    private void validateTelemetryAccess(Long vehicleId, Long userId) {
        boolean isOwner = externalListingsService.isVehicleOwner(vehicleId, userId);
        boolean hasActiveBooking = externalBookingService.hasTrackingPermission(userId, vehicleId);

        if (!isOwner && !hasActiveBooking) {
            throw new UnauthorizedAccessException(
                    "Not authorized to view telemetry for vehicle " + vehicleId);
        }
    }

    /**
     * Retrieves all telemetry data for a vehicle, sorted by creation date descending.
     * This ensures the most recent data appears first.
     * Validates authorization before returning data.
     *
     * @param query The query containing the vehicle ID
     * @param userId The ID of the authenticated user
     * @return List of telemetry records sorted by timestamp (most recent first)
     * @throws UnauthorizedAccessException if user is not authorized
     */
    @Override
    public List<Telemetry> handle(GetTelemetryByVehicleIdQuery query, Long userId) {
        validateTelemetryAccess(query.vehicleId(), userId);

        return telemetryRepository.findByVehicleId(
            query.vehicleId(), 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    /**
     * Retrieves the most recent telemetry record for a vehicle.
     * Validates authorization before returning data.
     *
     * @param query The query containing the vehicle ID
     * @param userId The ID of the authenticated user
     * @return An Optional containing the latest telemetry if found, empty otherwise
     * @throws UnauthorizedAccessException if user is not authorized
     */
    @Override
    public Optional<Telemetry> handle(GetLatestTelemetryQuery query, Long userId) {
        validateTelemetryAccess(query.vehicleId(), userId);

        return telemetryRepository.findFirstByVehicleIdOrderByCreatedAtDesc(query.vehicleId());
    }
}
