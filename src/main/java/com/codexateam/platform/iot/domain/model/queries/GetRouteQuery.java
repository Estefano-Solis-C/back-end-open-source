package com.codexateam.platform.iot.domain.model.queries;
/**
 * Query to retrieve route coordinates between two geographic points.
 * @param startLat Starting latitude
 * @param startLng Starting longitude
 * @param endLat Ending latitude
 * @param endLng Ending longitude
 */
public record GetRouteQuery(
        Double startLat,
        Double startLng,
        Double endLat,
        Double endLng
) {
}