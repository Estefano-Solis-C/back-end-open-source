package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iot.domain.exceptions.RouteNotFoundException;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.codexateam.platform.iot.interfaces.rest.resources.RouteCoordinateResource;
import com.codexateam.platform.iot.interfaces.rest.resources.CompleteRouteResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST controller exposing vehicle route geometry using OpenRouteService.
 */
@RestController
@RequestMapping("/api/v1/simulation")
@CrossOrigin(origins = "*")
public class RouteController {

    private final OpenRouteServiceApiClient openRouteServiceApiClient;

    public RouteController(OpenRouteServiceApiClient openRouteServiceApiClient) {
        this.openRouteServiceApiClient = openRouteServiceApiClient;
    }

    /**
     * GET /api/v1/simulation/route
     * Params: startLat, startLng, endLat, endLng
     * Returns: List<RouteCoordinateResource> with full geometry
     */
    @GetMapping("/route")
    public ResponseEntity<List<RouteCoordinateResource>> getRoute(
            @RequestParam("startLat") Double startLat,
            @RequestParam("startLng") Double startLng,
            @RequestParam("endLat") Double endLat,
            @RequestParam("endLng") Double endLng
    ) {
        // Basic validation
        if (startLat == null || startLng == null || endLat == null || endLng == null) {
            throw new IllegalArgumentException("Coordinates must not be null");
        }
        if (startLat < -90.0 || startLat > 90.0 || endLat < -90.0 || endLat > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (startLng < -180.0 || startLng > 180.0 || endLng < -180.0 || endLng > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (!openRouteServiceApiClient.isConfigured()) {
            throw new RouteNotFoundException("OpenRouteService not configured");
        }

        List<double[]> coordinates = openRouteServiceApiClient.getRouteCoordinates(startLat, startLng, endLat, endLng);
        if (coordinates == null || coordinates.isEmpty()) {
            throw new RouteNotFoundException("Route not found");
        }

        List<RouteCoordinateResource> body = new ArrayList<>(coordinates.size());
        for (double[] coord : coordinates) {
            if (coord != null && coord.length >= 2) {
                body.add(new RouteCoordinateResource(coord[0], coord[1]));
            }
        }
        return ResponseEntity.ok(body);
    }

    /**
     * GET /api/v1/simulation/complete-route
     * Params: startLat, startLng, endLat, endLng
     * Returns: CompleteRouteResource with coordinates, distance, duration, and convenience fields
     */
    @GetMapping("/complete-route")
    public ResponseEntity<CompleteRouteResource> getCompleteRoute(
            @RequestParam("startLat") Double startLat,
            @RequestParam("startLng") Double startLng,
            @RequestParam("endLat") Double endLat,
            @RequestParam("endLng") Double endLng
    ) {
        // Basic validation
        if (startLat == null || startLng == null || endLat == null || endLng == null) {
            throw new IllegalArgumentException("Coordinates must not be null");
        }
        if (startLat < -90.0 || startLat > 90.0 || endLat < -90.0 || endLat > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90");
        }
        if (startLng < -180.0 || startLng > 180.0 || endLng < -180.0 || endLng > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180");
        }

        if (!openRouteServiceApiClient.isConfigured()) {
            throw new RouteNotFoundException("OpenRouteService not configured");
        }

        RouteResponse route = openRouteServiceApiClient.getCompleteRoute(startLat, startLng, endLat, endLng);
        if (route == null || route.getCoordinates() == null || route.getCoordinates().isEmpty()) {
            throw new RouteNotFoundException("Route not found");
        }

        // Map coordinates to List<List<Double>>
        List<List<Double>> coords = new ArrayList<>(route.getCoordinates().size());
        for (double[] c : route.getCoordinates()) {
            if (c != null && c.length >= 2) {
                List<Double> pair = new ArrayList<>(2);
                pair.add(c[0]); // lat
                pair.add(c[1]); // lng
                coords.add(pair);
            }
        }

        Double distanceMeters = route.getDistanceMeters();
        Double durationSeconds = route.getDurationSeconds();
        Double distanceKm = route.getDistanceKilometers();
        Double durationMinutes = route.getDurationMinutes();
        Double averageSpeedKmh = route.getAverageSpeedKmh();

        CompleteRouteResource resource = new CompleteRouteResource(
                coords,
                distanceMeters,
                durationSeconds,
                distanceKm,
                durationMinutes,
                averageSpeedKmh
        );

        return ResponseEntity.ok(resource);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(RouteNotFoundException.class)
    public ResponseEntity<String> handleRouteNotFoundException(RouteNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("unexpected error: " + ex.getMessage());
    }
}
