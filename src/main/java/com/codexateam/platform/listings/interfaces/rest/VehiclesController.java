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
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for the Listings bounded context.
 * Handles all API requests related to Vehicles.
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
     * Creates a new vehicle listing.
     * Requires ARRENDADOR role.
     * @param resource The vehicle data.
     * @return The created vehicle resource.
     */
    @PostMapping
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    public ResponseEntity<VehicleResource> createVehicle(@RequestBody CreateVehicleResource resource) {
        Long ownerId = getAuthenticatedUserId();
        var command = CreateVehicleCommandFromResourceAssembler.toCommandFromResource(resource, ownerId);
        var vehicle = vehicleCommandService.handle(command)
                .orElseThrow(() -> new RuntimeException("Error creating vehicle"));
        
        var vehicleResource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleResource);
    }

    /**
     * Gets all vehicle listings. Publicly accessible.
     * @return A list of all vehicle resources.
     */
    @GetMapping
    public ResponseEntity<List<VehicleResource>> getAllVehicles() {
        var query = new GetAllVehiclesQuery();
        var vehicles = vehicleQueryService.handle(query);
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }

    /**
     * Gets a single vehicle by its ID. Publicly accessible.
     * @param vehicleId The ID of the vehicle.
     * @return The vehicle resource.
     */
    @GetMapping("/{vehicleId}")
    public ResponseEntity<VehicleResource> getVehicleById(@PathVariable Long vehicleId) {
        var query = new GetVehicleByIdQuery(vehicleId);
        var vehicle = vehicleQueryService.handle(query)
                .orElseThrow(() -> new RuntimeException("Vehicle not found"));
        var resource = VehicleResourceFromEntityAssembler.toResourceFromEntity(vehicle);
        return ResponseEntity.ok(resource);
    }
    
    /**
     * Gets all vehicles listed by the authenticated owner.
     * Requires ARRENDADOR role.
     * @return A list of the owner's vehicle resources.
     */
    @GetMapping("/my-listings")
    @PreAuthorize("hasRole('ROLE_ARRENDADOR')")
    public ResponseEntity<List<VehicleResource>> getMyListings() {
        Long ownerId = getAuthenticatedUserId();
        var query = new GetVehiclesByOwnerIdQuery(ownerId);
        var vehicles = vehicleQueryService.handle(query);
        var resources = vehicles.stream()
                .map(VehicleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}
