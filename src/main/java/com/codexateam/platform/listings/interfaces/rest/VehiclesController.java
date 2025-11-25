package com.codexateam.platform.listings.interfaces.rest;

import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.listings.domain.model.queries.GetAllVehiclesQuery;
import com.codexateam.platform.listings.domain.model.queries.GetVehicleByIdQuery;
import com.codexateam.platform.listings.domain.model.queries.GetVehiclesByOwnerIdQuery;
import com.codexateam.platform.listings.domain.services.VehicleCommandService;
import com.codexateam.platform.listings.domain.services.VehicleQueryService;
import com.codexateam.platform.listings.interfaces.rest.resources.CreateVehicleResource;
import com.codexateam.platform.listings.interfaces.rest.resources.VehicleResource;
import com.codexateam.platform.listings.interfaces.rest.transform.CreateVehicleCommandFromResourceAssembler;
import com.codexateam.platform.listings.interfaces.rest.transform.VehicleResourceFromEntityAssembler;
import com.codexateam.platform.listings.domain.model.commands.UpdateVehicleCommand;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.codexateam.platform.listings.domain.model.commands.DeleteVehicleCommand;

/**
 * REST Controller for the Listings bounded context.
 * Provides endpoints to create, update, list, retrieve and delete vehicles.
 */
@RestController
@RequestMapping("/api/v1/vehicles")
@Tag(name = "Vehicles", description = "Endpoints for managing vehicle listings")
public class VehiclesController {

    private final VehicleCommandService vehicleCommandService;
    private final VehicleQueryService vehicleQueryService;

    public VehiclesController(VehicleCommandService vehicleCommandService, VehicleQueryService vehicleQueryService) {
        this.vehicleCommandService = vehicleCommandService;
        this.vehicleQueryService = vehicleQueryService;
    }
    
    /**
     * Extracts the authenticated user's ID from the security context.
     * @return the authenticated user ID
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
     * Creates a new vehicle listing (owner only).
     * @param resource payload containing vehicle attributes
     * @return created vehicle resource with HTTP 201
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Create Vehicle Listing", description = "Create a new vehicle listing as an owner (ROLE_ARRENDADOR)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Vehicle created"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<VehicleResource> createVehicle(@RequestBody CreateVehicleResource resource) {
        Long ownerId = getAuthenticatedUserId();
        var command = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource, ownerId);
        var vehicle = vehicleCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Error creating vehicle"));
        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleResource);
    }

    /**
     * Retrieves all vehicles (public).
     * @return list of vehicle resources
     */
    @GetMapping
    @Operation(summary = "Get All Vehicles", description = "Get a list of all available vehicles (Public)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicles found")
    })
    public ResponseEntity<List<VehicleResource>> getAllVehicles() {
        var query = new GetAllVehiclesQuery();
        var vehicles = vehicleQueryService.handle(query);
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves a vehicle by ID (public).
     * @param vehicleId identifier of the vehicle
     * @return vehicle resource if found
     */
    @GetMapping("/{vehicleId}")
    @Operation(summary = "Get Vehicle by ID", description = "Get details for a specific vehicle (Public)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle found"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable Long vehicleId) {
        var query = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(query)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        var resource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle);
        return ResponseEntity.ok(resource);
    }
    
    /**
     * Retrieves all vehicles owned by the authenticated owner.
     * @return list of owner's vehicle resources
     */
    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Get Owner's Listings", description = "Get all vehicles listed by the authenticated owner (ROLE_ARRENDADOR)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listings found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<VehicleResource>> getMyListings() {
        Long ownerId = getAuthenticatedUserId();
        var query = new GetVehiclesByOwnerIdQuery(ownerId);
        var vehicles = vehicleQueryService.handle(query);
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Updates an existing vehicle listing (owner only).
     * @param vehicleId vehicle identifier
     * @param resource updated data
     * @return updated vehicle resource
     */
    @PutMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Update Vehicle", description = "Update an existing vehicle listing (Owner only)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Vehicle updated successfully"),
            @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    public ResponseEntity<VehicleResource> updateVehicle(@PathVariable Long vehicleId, @RequestBody CreateVehicleResource resource) {
        var command = new UpdateVehicleCommand(
                vehicleId,
                resource.brand(),
                resource.model(),
                resource.year(),
                resource.pricePerDay(),
                resource.imageUrl()
        );
        var updatedVehicle = vehicleCommandService.handle(command);
        if (updatedVehicle.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(updatedVehicle.get());
        return ResponseEntity.ok(vehicleResource);
    }

    /**
     * Deletes a vehicle with cascade cleanup (owner only).
     * @param vehicleId vehicle identifier
     * @return empty 204 response
     */
    @DeleteMapping("/{vehicleId}")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    @Operation(summary = "Delete Vehicle", description = "Delete a vehicle listing (Owner only) with cascade cleanup")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Vehicle deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> deleteVehicle(@PathVariable Long vehicleId) {
        var command = new DeleteVehicleCommand(vehicleId);
        vehicleCommandService.handle(command);
        return ResponseEntity.noContent().build();
    }
}
