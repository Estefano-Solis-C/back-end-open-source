package com.codexateam.platform.iot.application.internal.queryservices;

import com.codexateam.platform.iot.domain.exceptions.RouteNotFoundException;
import com.codexateam.platform.iot.domain.model.queries.GetRouteQuery;
import com.codexateam.platform.iot.domain.model.queries.GetCompleteRouteQuery;
import com.codexateam.platform.iot.domain.services.RouteQueryService;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.codexateam.platform.shared.domain.exceptions.ValidationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementation of RouteQueryService.
 * Handles all route-related queries following CQRS pattern.
 * Includes validation and delegates to external routing service.
 */
@Service
public class RouteQueryServiceImpl implements RouteQueryService {

    private final OpenRouteServiceApiClient openRouteServiceApiClient;

    public RouteQueryServiceImpl(OpenRouteServiceApiClient openRouteServiceApiClient) {
        this.openRouteServiceApiClient = openRouteServiceApiClient;
    }

    /**
     * Validates geographic coordinates.
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @throws ValidationException if any coordinate is invalid
     */
    private void validateCoordinates(Double startLat, Double startLng, Double endLat, Double endLng) {
        if (startLat == null || startLng == null || endLat == null || endLng == null) {
            throw new ValidationException("All coordinates must be provided");
        }
        if (startLat < -90.0 || startLat > 90.0 || endLat < -90.0 || endLat > 90.0) {
            throw new ValidationException("Latitude must be between -90 and 90");
        }
        if (startLng < -180.0 || startLng > 180.0 || endLng < -180.0 || endLng > 180.0) {
            throw new ValidationException("Longitude must be between -180 and 180");
        }
    }

    /**
     * Retrieves route coordinates between two points.
     * Validates input and checks service availability.
     */
    @Override
    public List<double[]> handle(GetRouteQuery query) {
        validateCoordinates(query.startLat(), query.startLng(), query.endLat(), query.endLng());

        if (!openRouteServiceApiClient.isConfigured()) {
            throw new RouteNotFoundException("Routing service is not configured");
        }

        List<double[]> coordinates = openRouteServiceApiClient.getRouteCoordinates(
                query.startLat(), query.startLng(), query.endLat(), query.endLng()
        );

        if (coordinates == null || coordinates.isEmpty()) {
            throw new RouteNotFoundException("No route found between the specified coordinates");
        }

        return coordinates;
    }

    /**
     * Retrieves complete route information including distance and duration.
     * Validates input and checks service availability.
     */
    @Override
    public RouteResponse handle(GetCompleteRouteQuery query) {
        validateCoordinates(query.startLat(), query.startLng(), query.endLat(), query.endLng());

        if (!openRouteServiceApiClient.isConfigured()) {
            throw new RouteNotFoundException("Routing service is not configured");
        }

        RouteResponse route = openRouteServiceApiClient.getCompleteRoute(
                query.startLat(), query.startLng(), query.endLat(), query.endLng()
        );

        if (route == null || route.getCoordinates() == null || route.getCoordinates().isEmpty()) {
            throw new RouteNotFoundException("No route found between the specified coordinates");
        }

        return route;
    }
}

