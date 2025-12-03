package com.codexateam.platform.iot.domain.model.queries;
/**
 * Query to retrieve complete route information including coordinates, distance, and duration.
 * @param startLat Starting latitude
 * @param startLng Starting longitude
 * @param endLat Ending latitude
 * @param endLng Ending longitude
 */
public record GetCompleteRouteQuery(
        Double startLat,
        Double startLng,
        Double endLat,
        Double endLng
) {
}