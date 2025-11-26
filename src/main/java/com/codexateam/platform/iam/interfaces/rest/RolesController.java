package com.codexateam.platform.iam.interfaces.rest;

import com.codexateam.platform.iam.domain.model.queries.GetAllRolesQuery;
import com.codexateam.platform.iam.domain.services.RoleQueryService;
import com.codexateam.platform.iam.interfaces.rest.resources.RoleResource;
import com.codexateam.platform.iam.interfaces.rest.transform.RoleResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST Controller for managing roles.
 * Provides endpoints to list available roles in the system.
 */
@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Roles", description = "Endpoints for managing roles")
public class RolesController {

    private final RoleQueryService roleQueryService;

    public RolesController(RoleQueryService roleQueryService) {
        this.roleQueryService = roleQueryService;
    }

    /**
     * Retrieves all available roles in the system.
     * @return list of role resources
     */
    @Operation(summary = "Get All Roles", description = "Get a list of all available roles in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Roles retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<RoleResource>> getAllRoles() {
        var roles = roleQueryService.handle(new GetAllRolesQuery());
        var resources = roles.stream()
                .map(RoleResourceFromEntityAssembler::toResourceFromEntity)
                .toList();
        return ResponseEntity.ok(resources);
    }
}

