package com.codexateam.platform.iot.interfaces.rest.resources;

/**
 * Resource representing a single coordinate point in a route.
 * Used for route simulation and visualization in the frontend.
 *
 * @param lat Latitude of the coordinate point
 * @param lng Longitude of the coordinate point
 */
public record RouteCoordinateResource(
        Double lat,
        Double lng
) {
}


