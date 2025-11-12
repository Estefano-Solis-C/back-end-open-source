package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalBookingService;
import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.services.TelemetryCommandService;
import com.codexateam.platform.iot.domain.services.TelemetryQueryService;
import com.codexateam.platform.iot.interfaces.rest.resources.RecordTelemetryResource;
import com.codexateam.platform.iot.interfaces.rest.resources.TelemetryResource;
import com.codexateam.platform.iot.interfaces.rest.transform.RecordTelemetryCommandFromResourceAssembler;
import com.codexateam.platform.iot.interfaces.rest.transform.TelemetryResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the IoT bounded context.
 * Handles API requests for recording and viewing vehicle telemetry.
 *
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry", description = "Endpoints for IoT device telemetry")
public class TelemetryController {

    private final TelemetryCommandService telemetryCommandService;
    private final TelemetryQueryService telemetryQueryService;
    private final ExternalListingsService externalListingsService;
    private final ExternalBookingService externalBookingService;

    public TelemetryController(
            TelemetryCommandService telemetryCommandService,
            TelemetryQueryService telemetryQueryService,
            ExternalListingsService externalListingsService,
            ExternalBookingService externalBookingService) {
        this.telemetryCommandService = telemetryCommandService;
        this.telemetryQueryService = telemetryQueryService;
        this.externalListingsService = externalListingsService;
        this.externalBookingService = externalBookingService;
    }
    
    /**
     * Extracts the authenticated user's ID from the security context.
     * @return The authenticated user's ID.
     */
    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Records a new telemetry data point.
     * This endpoint would typically be secured for devices or vehicle owners.
     * @param resource The telemetry data.
     * @return The created telemetry resource.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    public ResponseEntity<TelemetryResource> recordTelemetry(@RequestBody RecordTelemetryResource resource) {
        Long ownerId = getAuthenticatedUserId();

        // Validate that the authenticated user is the owner of the vehicle
        if (!externalListingsService.isVehicleOwner(resource.vehicleId(), ownerId)) {
            throw new SecurityException("You are not authorized to record telemetry for this vehicle.");
        }

        var command = RecordTelemetryCommandFromResourceAssembler.toCommandFromResource(resource);
        var telemetry = telemetryCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Error recording telemetry data"));
        
        var telemetryResource = TelemetryResourceFromEntityAssembler.toResourceFromEntity(telemetry);
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryResource);
    }

    /**
     * Gets all telemetry data for a specific vehicle, sorted by most recent first.
     * Secured for both Arrendador (owner) and Arrendatario (renter).
     *
     * @param vehicleId The ID of the vehicle.
     * @return A list of telemetry resources.
     */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR') or hasRole('ROLE_ARRENDATARIO')")
    public ResponseEntity<List<TelemetryResource>> getTelemetryByVehicleId(@PathVariable Long vehicleId) {
        Long userId = getAuthenticatedUserId();

        // Validate that the authenticated user has permission to view this vehicle's tracking
        // User must be either:
        // 1. The owner of the vehicle (ARRENDADOR), OR
        // 2. A renter with an active booking for this vehicle (ARRENDATARIO)
        boolean isOwner = externalListingsService.isVehicleOwner(vehicleId, userId);
        boolean hasActiveBooking = externalBookingService.hasTrackingPermission(userId, vehicleId);

        if (!isOwner && !hasActiveBooking) {
            throw new SecurityException("You are not authorized to view tracking data for this vehicle.");
        }

        var query = new GetTelemetryByVehicleIdQuery(vehicleId);
        var telemetryList = telemetryQueryService.handle(query);
        var resources = telemetryList.stream()
                .map(TelemetryResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
