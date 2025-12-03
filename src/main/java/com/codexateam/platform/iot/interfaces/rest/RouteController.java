package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iot.domain.model.queries.GetRouteQuery;
import com.codexateam.platform.iot.domain.model.queries.GetCompleteRouteQuery;
import com.codexateam.platform.iot.domain.services.RouteQueryService;
import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.codexateam.platform.iot.interfaces.rest.resources.RouteCoordinateResource;
import com.codexateam.platform.iot.interfaces.rest.resources.CompleteRouteResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller exposing vehicle route geometry using OpenRouteService.
 * Follows REST best practices with clean separation of concerns.
 * All business logic and validation is delegated to the service layer.
 */
@RestController
@RequestMapping("/api/v1/simulation")
@CrossOrigin(origins = "*")
@Tag(name = "Route Simulation", description = "Endpoints for route planning and simulation")
public class RouteController {

    private final RouteQueryService routeQueryService;

    public RouteController(RouteQueryService routeQueryService) {
        this.routeQueryService = routeQueryService;
    }

    /**
     * Retrieves route coordinates between two geographic points.
     * All validation is handled by the service layer.
     *
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return List of route coordinates
     */
    @GetMapping("/route")
    @Operation(summary = "Get Route Coordinates", description = "Get route geometry between two points")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    public ResponseEntity<List<RouteCoordinateResource>> getRoute(
            @RequestParam("startLat") Double startLat,
            @RequestParam("startLng") Double startLng,
            @RequestParam("endLat") Double endLat,
            @RequestParam("endLng") Double endLng
    ) {
        var query = new GetRouteQuery(startLat, startLng, endLat, endLng);
        List<double[]> coordinates = routeQueryService.handle(query);

        List<RouteCoordinateResource> resources = new ArrayList<>(coordinates.size());
        for (double[] coord : coordinates) {
            if (coord != null && coord.length >= 2) {
                resources.add(new RouteCoordinateResource(coord[0], coord[1]));
            }
        }

        return ResponseEntity.ok(resources);
    }

    /**
     * Retrieves complete route information including coordinates, distance, and duration.
     * All validation is handled by the service layer.
     *
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return Complete route information
     */
    @GetMapping("/complete-route")
    @Operation(summary = "Get Complete Route", description = "Get complete route information with distance and duration")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Route found successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid coordinates"),
            @ApiResponse(responseCode = "404", description = "Route not found")
    })
    public ResponseEntity<CompleteRouteResource> getCompleteRoute(
            @RequestParam("startLat") Double startLat,
            @RequestParam("startLng") Double startLng,
            @RequestParam("endLat") Double endLat,
            @RequestParam("endLng") Double endLng
    ) {
        var query = new GetCompleteRouteQuery(startLat, startLng, endLat, endLng);
        RouteResponse route = routeQueryService.handle(query);

        // Transform coordinates to List<List<Double>> for JSON serialization
        List<List<Double>> coords = new ArrayList<>(route.getCoordinates().size());
        for (double[] c : route.getCoordinates()) {
            if (c != null && c.length >= 2) {
                coords.add(List.of(c[0], c[1]));
            }
        }

        CompleteRouteResource resource = new CompleteRouteResource(
                coords,
                route.getDistanceMeters(),
                route.getDurationSeconds(),
                route.getDistanceKilometers(),
                route.getDurationMinutes(),
                route.getAverageSpeedKmh()
        );

        return ResponseEntity.ok(resource);
    }
}
