package com.codexateam.platform.iot.infrastructure.external.dto;

import java.util.List;

/**
 * Data Transfer Object representing a complete route response from OpenRouteService.
 * Contains the route geometry (coordinates), total distance, and estimated duration.
 */
public class RouteResponse {
    private List<double[]> coordinates;
    private Double distanceMeters;
    private Double durationSeconds;

    public RouteResponse() {
    }

    public RouteResponse(List<double[]> coordinates, Double distanceMeters, Double durationSeconds) {
        this.coordinates = coordinates;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
    }

    /**
     * Gets the list of coordinate points that form the route geometry.
     * Each coordinate is an array of [latitude, longitude].
     *
     * @return List of coordinate arrays
     */
    public List<double[]> getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the route coordinates.
     *
     * @param coordinates List of coordinate arrays [latitude, longitude]
     */
    public void setCoordinates(List<double[]> coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Gets the total distance of the route in meters.
     *
     * @return Distance in meters
     */
    public Double getDistanceMeters() {
        return distanceMeters;
    }

    /**
     * Sets the route distance.
     *
     * @param distanceMeters Distance in meters
     */
    public void setDistanceMeters(Double distanceMeters) {
        this.distanceMeters = distanceMeters;
    }

    /**
     * Gets the estimated duration of the route in seconds.
     *
     * @return Duration in seconds
     */
    public Double getDurationSeconds() {
        return durationSeconds;
    }

    /**
     * Sets the route duration.
     *
     * @param durationSeconds Duration in seconds
     */
    public void setDurationSeconds(Double durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    /**
     * Gets the distance in kilometers (convenience method).
     *
     * @return Distance in kilometers
     */
    public Double getDistanceKilometers() {
        return distanceMeters != null ? distanceMeters / 1000.0 : null;
    }

    /**
     * Gets the duration in minutes (convenience method).
     *
     * @return Duration in minutes
     */
    public Double getDurationMinutes() {
        return durationSeconds != null ? durationSeconds / 60.0 : null;
    }

    /**
     * Gets the average speed in km/h (convenience method).
     *
     * @return Average speed in km/h, or null if distance or duration is missing
     */
    public Double getAverageSpeedKmh() {
        if (distanceMeters != null && durationSeconds != null && durationSeconds > 0) {
            return (distanceMeters / 1000.0) / (durationSeconds / 3600.0);
        }
        return null;
    }

    @Override
    public String toString() {
        return "RouteResponse{" +
                "coordinatesCount=" + (coordinates != null ? coordinates.size() : 0) +
                ", distanceMeters=" + distanceMeters +
                ", durationSeconds=" + durationSeconds +
                ", distanceKm=" + getDistanceKilometers() +
                ", durationMin=" + getDurationMinutes() +
                ", avgSpeedKmh=" + getAverageSpeedKmh() +
                '}';
    }
}

