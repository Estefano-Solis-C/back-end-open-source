package com.codexateam.platform.iam.interfaces.rest;

import com.codexateam.platform.iam.application.internal.outboundservices.tokens.TokenService;
import com.codexateam.platform.iam.domain.model.queries.GetRoleByNameQuery;
import com.codexateam.platform.iam.domain.model.valueobjects.Roles;
import com.codexateam.platform.iam.domain.services.RoleQueryService;
import com.codexateam.platform.iam.domain.services.UserCommandService;
import com.codexateam.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;
import com.codexateam.platform.iam.interfaces.rest.resources.SignInResource;
import com.codexateam.platform.iam.interfaces.rest.resources.SignUpResource;
import com.codexateam.platform.iam.interfaces.rest.resources.UserResource;
import com.codexateam.platform.iam.interfaces.rest.transform.AuthenticatedUserResourceFromEntityAssembler;
import com.codexateam.platform.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.codexateam.platform.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.codexateam.platform.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * REST Controller for Authentication endpoints (Sign-Up, Sign-In).
 */
@RestController
@RequestMapping("/api/v1/authentication")
@Tag(name = "Authentication", description = "Endpoints for user authentication (Sign-Up, Sign-In)")
public class AuthenticationController {

    private final UserCommandService userCommandService;
    private final RoleQueryService roleQueryService;
    private final TokenService tokenService;

    public AuthenticationController(UserCommandService userCommandService, RoleQueryService roleQueryService, TokenService tokenService) {
        this.userCommandService = userCommandService;
        this.roleQueryService = roleQueryService;
        this.tokenService = tokenService;
    }

    /**
     * Handles the POST request for user sign-up.
     * @param resource The sign-up data (name, email, password, role).
     * @return A ResponseEntity with the created UserResource or an error.
     */
    @PostMapping("/sign-up")
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource resource) {
        // Convert role string from db.json ('arrendador') to Enum ('ROLE_ARRENDADOR')
        Roles roleEnum;
        if ("arrendador".equalsIgnoreCase(resource.role())) {
            roleEnum = Roles.ROLE_ARRENDADOR;
        } else if ("arrendatario".equalsIgnoreCase(resource.role())) {
            roleEnum = Roles.ROLE_ARRENDATARIO;
        } else {
            return ResponseEntity.badRequest().build();
        }
        var role = roleQueryService.handle(new GetRoleByNameQuery(roleEnum))
                .orElseThrow(() -> new RuntimeException("Role not found: " + resource.role()));
        try {
            var command = SignUpCommandFromResourceAssembler.toCommandFromResource(resource, Set.of(role));
            var user = userCommandService.handle(command)
                    .orElseThrow(() -> new RuntimeException("Error creating user"));
            var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(userResource);
        } catch (IllegalArgumentException ex) {
            var body = new HashMap<String, Object>();
            body.put("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Handles the POST request for user sign-in.
     * @param resource The sign-in data (email, password).
     * @return A ResponseEntity with the AuthenticatedUserResource (including token) or an error.
     */
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInResource resource) {
        try {
            var command = SignInCommandFromResourceAssembler.toCommandFromResource(resource);
            var user = userCommandService.handle(command)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

            var token = tokenService.generateToken(user.getEmail());
            var authenticatedUserResource = AuthenticatedUserResourceFromEntityAssembler.toResourceFromEntity(user, token);
            return ResponseEntity.ok(authenticatedUserResource);
        } catch (IllegalArgumentException ex) {
            var body = new HashMap<String, Object>();
            body.put("error", "Invalid email or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }

    /**
     * Public GET helper to describe how to use the authentication endpoints.
     * This prevents a 405 when opening the base URL in a browser with GET.
     */
    @GetMapping
    public Map<String, Object> info() {
        var response = new java.util.HashMap<String, Object>();
        response.put("message", "Authentication API");
        response.put("note", "Use POST for sign-up and sign-in. This GET is informational.");
        var endpoints = new java.util.HashMap<String, String>();
        endpoints.put("signUp", "POST /api/v1/authentication/sign-up");
        endpoints.put("signIn", "POST /api/v1/authentication/sign-in");
        response.put("endpoints", endpoints);
        var examples = new java.util.HashMap<String, Object>();
        examples.put("signUpBody", java.util.Map.of(
                "name", "John Doe",
                "email", "john@example.com",
                "password", "yourPassword",
                "role", "arrendador|arrendatario"
        ));
        examples.put("signInBody", java.util.Map.of(
                "email", "john@example.com",
                "password", "yourPassword"
        ));
        response.put("examples", examples);
        return response;
    }
}
