package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iot.domain.exceptions.RouteNotFoundException;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.codexateam.platform.iot.interfaces.rest.resources.CompleteRouteResource;
import com.codexateam.platform.iot.interfaces.rest.resources.RouteCoordinateResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for route simulation endpoints.
 * Provides functionality to retrieve route coordinates between two geographic points
 * using the OpenRouteService API.
 */
@RestController
@RequestMapping("/api/v1/simulation")
@Tag(name = "Route Simulation", description = "Endpoints for route simulation and coordinate retrieval")
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);
    private static final String ERROR_ROUTE_NOT_FOUND = "Unable to retrieve route coordinates for the specified locations";
    private static final String ERROR_SERVICE_NOT_CONFIGURED = "Routing service is not properly configured";
    private static final String ERROR_INVALID_COORDINATES = "Invalid coordinates provided";

    private final OpenRouteServiceApiClient openRouteServiceApiClient;

    /**
     * Constructor with dependency injection.
     *
     * @param openRouteServiceApiClient Client for accessing the OpenRouteService API
     */
    public RouteController(OpenRouteServiceApiClient openRouteServiceApiClient) {
        this.openRouteServiceApiClient = openRouteServiceApiClient;
    }

    /**
     * Retrieves route coordinates between two geographic points.
     * The coordinates are returned in a format easily consumable by frontend applications.
     *
     * @param startLat Starting point latitude (must be between -90 and 90)
     * @param startLng Starting point longitude (must be between -180 and 180)
     * @param endLat Ending point latitude (must be between -90 and 90)
     * @param endLng Ending point longitude (must be between -180 and 180)
     * @return List of coordinate resources representing the route path
     */
    @GetMapping("/route")
    @Operation(
            summary = "Get route coordinates",
            description = "Retrieves a list of geographic coordinates representing the optimal route between two points"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Route successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RouteCoordinateResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid coordinates provided",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Route not found for the specified coordinates",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or external service unavailable",
                    content = @Content
            )
    })
    public ResponseEntity<List<RouteCoordinateResource>> getRoute(
            @Parameter(description = "Starting point latitude", required = true, example = "-12.046374")
            @RequestParam Double startLat,
            
            @Parameter(description = "Starting point longitude", required = true, example = "-77.042793")
            @RequestParam Double startLng,
            
            @Parameter(description = "Ending point latitude", required = true, example = "-12.056189")
            @RequestParam Double endLat,
            
            @Parameter(description = "Ending point longitude", required = true, example = "-77.029317")
            @RequestParam Double endLng
    ) {
        logger.info("Received route request: start({}, {}) -> end({}, {})", 
                    startLat, startLng, endLat, endLng);

        // Validate coordinates
        if (isInvalidCoordinate(startLat, startLng) || isInvalidCoordinate(endLat, endLng)) {
            logger.error("Invalid coordinates provided: start({}, {}) -> end({}, {})", 
                        startLat, startLng, endLat, endLng);
            throw new IllegalArgumentException(ERROR_INVALID_COORDINATES);
        }

        // Check if the service is configured
        if (!openRouteServiceApiClient.isConfigured()) {
            logger.error("OpenRouteService API client is not configured");
            throw new RouteNotFoundException(ERROR_SERVICE_NOT_CONFIGURED);
        }

        // Retrieve route coordinates from external service
        List<double[]> coordinates = openRouteServiceApiClient.getRouteCoordinates(
                startLat, startLng, endLat, endLng
        );

        // Check if coordinates were successfully retrieved
        if (coordinates == null || coordinates.isEmpty()) {
            logger.warn("No route coordinates returned from OpenRouteService");
            throw new RouteNotFoundException(ERROR_ROUTE_NOT_FOUND);
        }

        // Transform coordinates to resource format
        List<RouteCoordinateResource> routeResources = coordinates.stream()
                .map(coord -> new RouteCoordinateResource(coord[0], coord[1]))
                .collect(Collectors.toList());

        logger.info("Successfully retrieved {} coordinate points for route", routeResources.size());
        return ResponseEntity.ok(routeResources);
    }

    /**
     * Retrieves complete route information including coordinates, distance, and duration.
     * This endpoint returns the full geometry following actual streets and roads,
     * along with metrics useful for speed calculation and trip planning.
     *
     * @param startLat Starting point latitude (must be between -90 and 90)
     * @param startLng Starting point longitude (must be between -180 and 180)
     * @param endLat Ending point latitude (must be between -90 and 90)
     * @param endLng Ending point longitude (must be between -180 and 180)
     * @return Complete route information with coordinates, distance, and duration
     */
    @GetMapping("/route/complete")
    @Operation(
            summary = "Get complete route with distance and duration",
            description = "Retrieves the complete route geometry following actual streets, along with total distance and estimated duration"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Complete route successfully retrieved",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompleteRouteResource.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid coordinates provided",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Route not found for the specified coordinates",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error or external service unavailable",
                    content = @Content
            )
    })
    public ResponseEntity<CompleteRouteResource> getCompleteRoute(
            @Parameter(description = "Starting point latitude", required = true, example = "-12.046374")
            @RequestParam Double startLat,

            @Parameter(description = "Starting point longitude", required = true, example = "-77.042793")
            @RequestParam Double startLng,

            @Parameter(description = "Ending point latitude", required = true, example = "-12.056189")
            @RequestParam Double endLat,

            @Parameter(description = "Ending point longitude", required = true, example = "-77.029317")
            @RequestParam Double endLng
    ) {
        logger.info("Received complete route request: start({}, {}) -> end({}, {})",
                startLat, startLng, endLat, endLng);

        // Validate coordinates
        if (isInvalidCoordinate(startLat, startLng) || isInvalidCoordinate(endLat, endLng)) {
            logger.error("Invalid coordinates provided: start({}, {}) -> end({}, {})",
                    startLat, startLng, endLat, endLng);
            throw new IllegalArgumentException(ERROR_INVALID_COORDINATES);
        }

        // Check if the service is configured
        if (!openRouteServiceApiClient.isConfigured()) {
            logger.error("OpenRouteService API client is not configured");
            throw new RouteNotFoundException(ERROR_SERVICE_NOT_CONFIGURED);
        }

        // Retrieve complete route information from external service
        RouteResponse routeResponse = openRouteServiceApiClient.getCompleteRoute(
                startLat, startLng, endLat, endLng
        );

        // Check if route was successfully retrieved
        if (routeResponse == null || routeResponse.getCoordinates() == null || routeResponse.getCoordinates().isEmpty()) {
            logger.warn("No route information returned from OpenRouteService");
            throw new RouteNotFoundException(ERROR_ROUTE_NOT_FOUND);
        }

        // Transform coordinates from double[][] to List<List<Double>> for JSON serialization
        List<List<Double>> coordinatesList = routeResponse.getCoordinates().stream()
                .map(coord -> Arrays.asList(coord[0], coord[1]))
                .collect(Collectors.toList());

        // Create resource with all information
        CompleteRouteResource resource = new CompleteRouteResource(
                coordinatesList,
                routeResponse.getDistanceMeters(),
                routeResponse.getDurationSeconds(),
                routeResponse.getDistanceKilometers(),
                routeResponse.getDurationMinutes(),
                routeResponse.getAverageSpeedKmh()
        );

        logger.info("Successfully retrieved complete route: {} points, {} km, {} min",
                coordinatesList.size(),
                resource.distanceKm() != null ? String.format("%.2f", resource.distanceKm()) : "N/A",
                resource.durationMinutes() != null ? String.format("%.2f", resource.durationMinutes()) : "N/A");

        return ResponseEntity.ok(resource);
    }

    /**
     * Checks if geographic coordinates are invalid.
     *
     * @param lat Latitude value
     * @param lng Longitude value
     * @return true if coordinates are invalid, false otherwise
     */
    private boolean isInvalidCoordinate(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return true;
        }
        return lat < -90 || lat > 90 || lng < -180 || lng > 180;
    }

    /**
     * Exception handler for IllegalArgumentException.
     * Returns HTTP 400 Bad Request for invalid input.
     *
     * @param ex The exception
     * @return Error message with HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        logger.error("Invalid argument: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Exception handler for RouteNotFoundException.
     * Returns HTTP 404 Not Found when route cannot be retrieved.
     *
     * @param ex The exception
     * @return Error message with HTTP 404 status
     */
    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<String> handleRouteNotFoundException(RouteNotFoundException ex) {
        logger.error("Route not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Exception handler for generic exceptions.
     * Returns HTTP 500 Internal Server Error for unexpected errors.
     *
     * @param ex The exception
     * @return Error message with HTTP 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        logger.error("Unexpected error processing route request", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred while processing the route request");
    }
}

