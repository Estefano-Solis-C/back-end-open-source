package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalBookingService;
import com.codexateam.platform.iot.application.internal.outboundservices.acl.ExternalListingsService;
import com.codexateam.platform.iot.application.internal.services.TelemetrySimulatorService;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.services.TelemetryCommandService;
import com.codexateam.platform.iot.domain.services.TelemetryQueryService;
import com.codexateam.platform.iot.interfaces.rest.resources.RecordTelemetryResource;
import com.codexateam.platform.iot.interfaces.rest.resources.TelemetryResource;
import com.codexateam.platform.iot.interfaces.rest.transform.RecordTelemetryCommandFromResourceAssembler;
import com.codexateam.platform.iot.interfaces.rest.transform.TelemetryResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for the IoT bounded context.
 * Provides endpoints to record and retrieve vehicle telemetry.
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry", description = "Endpoints for IoT device telemetry")
public class TelemetryController {

    private final TelemetryCommandService telemetryCommandService;
    private final TelemetryQueryService telemetryQueryService;
    private final ExternalListingsService externalListingsService;
    private final ExternalBookingService externalBookingService;
    private final TelemetrySimulatorService telemetrySimulatorService;

    public TelemetryController(
            TelemetryCommandService telemetryCommandService,
            TelemetryQueryService telemetryQueryService,
            ExternalListingsService externalListingsService,
            ExternalBookingService externalBookingService,
            TelemetrySimulatorService telemetrySimulatorService) {
        this.telemetryCommandService = telemetryCommandService;
        this.telemetryQueryService = telemetryQueryService;
        this.externalListingsService = externalListingsService;
        this.externalBookingService = externalBookingService;
        this.telemetrySimulatorService = telemetrySimulatorService;
    }
    
    /**
     * Extracts authenticated user ID.
     * @return user id
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
     * Records telemetry for a vehicle (owner only).
     * @param resource telemetry payload
     * @return created telemetry resource
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Record Telemetry", description = "Record a new telemetry data point for a vehicle (ROLE_ARRENDADOR)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Telemetry recorded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (not owner)")
    })
    public ResponseEntity<TelemetryResource> recordTelemetry(@RequestBody RecordTelemetryResource resource) {
        Long ownerId = getAuthenticatedUserId();
        if (!externalListingsService.isVehicleOwner(resource.vehicleId(), ownerId)) {
            throw new SecurityException("Not authorized to record telemetry for this vehicle");
        }
        var command = RecordTelemetryCommandFromResourceAssembler.toCommandFromResource(resource);
        var telemetry = telemetryCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Error recording telemetry data"));
        var telemetryResource = TelemetryResourceFromEntityAssembler.toResourceFromEntity(telemetry);
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryResource);
    }

    /**
     * Retrieves telemetry for a vehicle (owner or renter with active booking).
     * @param vehicleId vehicle identifier
     * @return list of telemetry resources
     */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR') or hasRole('ROLE_ARRENDATARIO')")
    @Operation(summary = "Get Telemetry by Vehicle", description = "Get all telemetry data for a vehicle (Owner or active Renter)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Telemetry found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<List<TelemetryResource>> getTelemetryByVehicleId(@PathVariable Long vehicleId) {
        Long userId = getAuthenticatedUserId();
        boolean isOwner = externalListingsService.isVehicleOwner(vehicleId, userId);
        boolean hasActiveBooking = externalBookingService.hasTrackingPermission(userId, vehicleId);
        if (!isOwner && !hasActiveBooking) {
            throw new SecurityException("Not authorized to view telemetry for this vehicle");
        }
        var query = new GetTelemetryByVehicleIdQuery(vehicleId);
        var telemetryList = telemetryQueryService.handle(query);
        var resources = telemetryList.stream()
                .map(TelemetryResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Starts a realistic telemetry simulation for a vehicle (owner only).
     * The simulation runs asynchronously in the background and uses real road coordinates
     * from OpenRouteService API to simulate a vehicle traveling through Lima, Peru.
     *
     * @param vehicleId The ID of the vehicle to simulate
     * @return Response indicating the simulation has started
     */
    @PostMapping("/simulate/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Start Telemetry Simulation",
               description = "Start a realistic telemetry simulation for a vehicle using real road coordinates (ROLE_ARRENDADOR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Simulation started successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (not vehicle owner)")
    })
    public ResponseEntity<Map<String, String>> startTelemetrySimulation(@PathVariable Long vehicleId) {
        Long ownerId = getAuthenticatedUserId();

        // Verify that the authenticated user owns the vehicle
        if (!externalListingsService.isVehicleOwner(vehicleId, ownerId)) {
            throw new SecurityException("Not authorized to simulate telemetry for this vehicle");
        }

        // Start the asynchronous simulation
        telemetrySimulatorService.startSimulation(vehicleId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            Map.of(
                "message", "Telemetry simulation started for vehicle " + vehicleId,
                "status", "running",
                "vehicleId", vehicleId.toString(),
                "note", "Simulation is running asynchronously. Use GET /api/v1/telemetry/vehicle/" + vehicleId + " to view results."
            )
        );
    }

    /**
     * Starts a custom telemetry simulation with specified start and end coordinates.
     *
     * @param vehicleId The ID of the vehicle to simulate
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return Response indicating the simulation has started
     */
    @PostMapping("/simulate/{vehicleId}/custom")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Start Custom Telemetry Simulation",
               description = "Start a telemetry simulation with custom start and end coordinates (ROLE_ARRENDADOR only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Simulation started successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden (not vehicle owner)")
    })
    public ResponseEntity<Map<String, String>> startCustomTelemetrySimulation(
            @PathVariable Long vehicleId,
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng) {

        Long ownerId = getAuthenticatedUserId();

        if (!externalListingsService.isVehicleOwner(vehicleId, ownerId)) {
            throw new SecurityException("Not authorized to simulate telemetry for this vehicle");
        }

        telemetrySimulatorService.startSimulation(vehicleId, startLat, startLng, endLat, endLng);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            Map.of(
                "message", "Custom telemetry simulation started for vehicle " + vehicleId,
                "status", "running",
                "vehicleId", vehicleId.toString(),
                "route", String.format("(%.6f, %.6f) -> (%.6f, %.6f)", startLat, startLng, endLat, endLng),
                "note", "Simulation is running asynchronously. Use GET /api/v1/telemetry/vehicle/" + vehicleId + " to view results."
            )
        );
    }
}
