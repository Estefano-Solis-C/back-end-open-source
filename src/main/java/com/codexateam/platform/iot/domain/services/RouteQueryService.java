package com.codexateam.platform.iot.domain.services;

import com.codexateam.platform.iot.domain.model.queries.GetRouteQuery;
import com.codexateam.platform.iot.domain.model.queries.GetCompleteRouteQuery;
import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;

import java.util.List;

/**
 * Route Query Service interface following CQRS pattern.
 * Handles all route-related queries.
 */
public interface RouteQueryService {

    /**
     * Retrieves route coordinates between two points.
     * @param query The query containing start and end coordinates
     * @return List of coordinate pairs [latitude, longitude]
     */
    List<double[]> handle(GetRouteQuery query);

    /**
     * Retrieves complete route information including distance and duration.
     * @param query The query containing start and end coordinates
     * @return Complete route response with coordinates and metadata
     */
    RouteResponse handle(GetCompleteRouteQuery query);
}

