package com.codexateam.platform.iot.interfaces.rest.resources;

import java.util.List;

/**
 * Resource representing a complete route with coordinates, distance, and duration.
 * This format is optimized for frontend consumption.
 *
 * @param coordinates List of coordinate pairs [latitude, longitude] forming the route
 * @param distanceMeters Total distance of the route in meters
 * @param durationSeconds Estimated duration of the route in seconds
 * @param distanceKm Total distance in kilometers (convenience field)
 * @param durationMinutes Estimated duration in minutes (convenience field)
 * @param averageSpeedKmh Average speed in km/h (convenience field)
 */
public record CompleteRouteResource(
        List<List<Double>> coordinates,
        Double distanceMeters,
        Double durationSeconds,
        Double distanceKm,
        Double durationMinutes,
        Double averageSpeedKmh
) {
}

