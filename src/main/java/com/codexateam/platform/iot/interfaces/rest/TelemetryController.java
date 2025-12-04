package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.iot.application.internal.outboundservices.AutomaticTelemetryGeneratorService;
import com.codexateam.platform.iot.domain.model.queries.GetTelemetryByVehicleIdQuery;
import com.codexateam.platform.iot.domain.model.queries.GetLatestTelemetryQuery;
import com.codexateam.platform.iot.domain.services.TelemetryCommandService;
import com.codexateam.platform.iot.domain.services.TelemetryQueryService;
import com.codexateam.platform.iot.interfaces.rest.resources.RecordTelemetryResource;
import com.codexateam.platform.iot.interfaces.rest.resources.TelemetryResource;
import com.codexateam.platform.iot.interfaces.rest.transform.RecordTelemetryCommandFromResourceAssembler;
import com.codexateam.platform.iot.interfaces.rest.transform.TelemetryResourceFromEntityAssembler;
import com.codexateam.platform.booking.infrastructure.persistence.jpa.repositories.BookingRepository;
import com.codexateam.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.codexateam.platform.iam.domain.model.aggregates.User;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * REST Controller for the IoT bounded context.
 * Provides endpoints to record and retrieve vehicle telemetry.
 * This controller follows clean architecture principles:
 * - No business logic or validation (delegated to services)
 * - No exception handling (handled by GlobalExceptionHandler)
 * - Only HTTP concerns (request/response mapping)
 */
@RestController
@RequestMapping("/api/v1/telemetry")
@Tag(name = "Telemetry", description = "Endpoints for IoT device telemetry")
public class TelemetryController {

    private static final String BOOKING_STATUS_CONFIRMED = "CONFIRMED";
    private static final String DEFAULT_RENTER_NAME = "Not Assigned";

    private final TelemetryCommandService telemetryCommandService;
    private final TelemetryQueryService telemetryQueryService;
    private final com.codexateam.platform.iot.domain.services.SimulationCommandService simulationCommandService;
    private final AutomaticTelemetryGeneratorService automaticTelemetryGeneratorService;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    private final Map<Long, List<List<Double>>> routeCache = new ConcurrentHashMap<>();

    public TelemetryController(
            TelemetryCommandService telemetryCommandService,
            TelemetryQueryService telemetryQueryService,
            com.codexateam.platform.iot.domain.services.SimulationCommandService simulationCommandService,
            AutomaticTelemetryGeneratorService automaticTelemetryGeneratorService,
            BookingRepository bookingRepository,
            UserRepository userRepository) {
        this.telemetryCommandService = telemetryCommandService;
        this.telemetryQueryService = telemetryQueryService;
        this.simulationCommandService = simulationCommandService;
        this.automaticTelemetryGeneratorService = automaticTelemetryGeneratorService;
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Extracts authenticated user ID from security context.
     * @return Authenticated user ID
     * @throws SecurityException if user is not authenticated
     */
    private Long getAuthenticatedUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new SecurityException("User not authenticated");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return userDetails.getId();
    }

    /**
     * Records telemetry for a vehicle.
     * Only vehicle owners can record telemetry.
     * Input validation is automatic via @Valid annotation.
     * Authorization is handled in the service layer.
     *
     * @param resource Telemetry data to record
     * @return Created telemetry resource with 201 status
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Record Telemetry",
               description = "Record a new telemetry data point for a vehicle (Owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Telemetry recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not vehicle owner"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<TelemetryResource> recordTelemetry(@Valid @RequestBody RecordTelemetryResource resource) {
        Long userId = getAuthenticatedUserId();
        var command = RecordTelemetryCommandFromResourceAssembler.toCommandFromResource(resource);
        var telemetry = telemetryCommandService.handle(command, userId);
        var telemetryResource = TelemetryResourceFromEntityAssembler.toResourceFromEntity(telemetry);
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryResource);
    }

    /**
     * Retrieves all telemetry data for a vehicle.
     * Accessible by vehicle owner or renters with active bookings.
     * Authorization is handled in the service layer.
     *
     * @param vehicleId Vehicle identifier
     * @return List of telemetry records
     */
    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR') or hasRole('ROLE_ARRENDATARIO')")
    @Operation(summary = "Get Telemetry by Vehicle",
               description = "Get all telemetry data for a vehicle (Owner or active Renter)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Telemetry data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to view this vehicle's telemetry")
    })
    public ResponseEntity<List<TelemetryResource>> getTelemetryByVehicleId(@PathVariable Long vehicleId) {
        Long userId = getAuthenticatedUserId();
        var query = new GetTelemetryByVehicleIdQuery(vehicleId);
        var telemetryList = telemetryQueryService.handle(query, userId);
        var resources = telemetryList.stream()
                .map(TelemetryResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves the latest telemetry data point for a vehicle.
     * Includes planned route and renter information if available.
     * Accessible by vehicle owner or renters with active bookings.
     * Authorization is handled in the service layer.
     *
     * @param vehicleId Vehicle identifier
     * @return Latest telemetry resource or 404 if not found
     */
    @GetMapping("/vehicle/{vehicleId}/latest")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR') or hasRole('ROLE_ARRENDATARIO')")
    @Operation(summary = "Get Latest Telemetry",
               description = "Get the latest telemetry data point for a vehicle with route and renter information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Latest telemetry found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not authorized to view this vehicle's telemetry"),
            @ApiResponse(responseCode = "404", description = "No telemetry data found for this vehicle")
    })
    public ResponseEntity<TelemetryResource> getLatestTelemetryByVehicleId(@PathVariable Long vehicleId) {
        Long userId = getAuthenticatedUserId();

        automaticTelemetryGeneratorService.notifyActiveMonitoring(vehicleId);

        var query = new GetLatestTelemetryQuery(vehicleId);
        var optionalTelemetry = telemetryQueryService.handle(query, userId);

        if (optionalTelemetry.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<List<Double>> plannedRoute = routeCache.computeIfAbsent(vehicleId, id -> {
            var plannedRoutePoints = automaticTelemetryGeneratorService.getPlannedRoute(id);
            return plannedRoutePoints.stream().map(p -> List.of(p[0], p[1])).toList();
        });

        var resource = TelemetryResourceFromEntityAssembler
                .toResourceFromEntity(optionalTelemetry.get(), plannedRoute);

        Date now = new Date();
        bookingRepository.findFirstByVehicleIdAndBookingStatus_StatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                vehicleId, BOOKING_STATUS_CONFIRMED, now, now)
            .flatMap(booking -> userRepository.findById(booking.getRenterId()).map(User::getName))
            .ifPresent(resource::setRenterName);

        if (resource.getRenterName() == null) {
            resource.setRenterName(DEFAULT_RENTER_NAME);
        }

        return ResponseEntity.ok(resource);
    }

    /**
     * Starts a realistic telemetry simulation for a vehicle.
     * The simulation runs asynchronously using real road coordinates.
     * Only vehicle owners can start simulations.
     *
     * @param vehicleId The ID of the vehicle to simulate
     * @return Response indicating the simulation has started
     */
    @PostMapping("/simulate/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Start Telemetry Simulation",
               description = "Start a realistic telemetry simulation for a vehicle using real road coordinates (Owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Simulation started successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not vehicle owner"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Map<String, String>> startTelemetrySimulation(@PathVariable Long vehicleId) {
        Long userId = getAuthenticatedUserId();
        simulationCommandService.startSimulation(vehicleId, userId);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            Map.of(
                "message", "Telemetry simulation started for vehicle " + vehicleId,
                "status", "running",
                "vehicle_id", vehicleId.toString(),
                "note", "Simulation is running asynchronously. Use GET /api/v1/telemetry/vehicle/" + vehicleId + " to view results."
            )
        );
    }

    /**
     * Starts a custom telemetry simulation with specified start and end coordinates.
     * Only vehicle owners can start simulations.
     * Authorization is handled in the service layer.
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
               description = "Start a telemetry simulation with custom start and end coordinates (Owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Simulation started successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - not vehicle owner"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<Map<String, String>> startCustomTelemetrySimulation(
            @PathVariable Long vehicleId,
            @RequestParam double startLat,
            @RequestParam double startLng,
            @RequestParam double endLat,
            @RequestParam double endLng) {

        Long userId = getAuthenticatedUserId();
        simulationCommandService.startCustomSimulation(vehicleId, userId, startLat, startLng, endLat, endLng);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(
            Map.of(
                "message", "Custom telemetry simulation started for vehicle " + vehicleId,
                "status", "running",
                "vehicle_id", vehicleId.toString(),
                "route", String.format("(%.6f, %.6f) -> (%.6f, %.6f)", startLat, startLng, endLat, endLng),
                "note", "Simulation is running asynchronously. Use GET /api/v1/telemetry/vehicle/" + vehicleId + " to view results."
            )
        );
    }

    /**
     * Clears the route cache periodically to prevent memory leaks.
     * Runs once every 24 hours.
     */
    @Scheduled(fixedRate = 24L * 60L * 60L * 1000L)
    public void clearRouteCachePeriodically() {
        routeCache.clear();
    }
}
