package com.codexateam.platform.iot.infrastructure.external;

import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Spring component for consuming the OpenRouteService API to retrieve vehicle routing information.
 * This service is used to simulate realistic trips by fetching actual road coordinates
 * between two geographic points using RestTemplate.
 */
@Service
public class OpenRouteServiceApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouteServiceApiClient.class);
    private static final String BASE_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    @Value("${openrouteservice.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Constructor that injects RestTemplateBuilder to build the RestTemplate instance.
     *
     * @param restTemplateBuilder Builder for creating RestTemplate with proper configuration
     */
    public OpenRouteServiceApiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Retrieves route coordinates from OpenRouteService API.
     *
     * Note: The API expects coordinates in longitude,latitude order, but this method
     * accepts and returns coordinates in latitude,longitude order for consistency with the system.
     *
     * @param startLat Starting point latitude
     * @param startLng Starting point longitude
     * @param endLat Ending point latitude
     * @param endLng Ending point longitude
     * @return List of coordinate pairs [latitude, longitude] representing the route.
     *         Returns empty list if the request fails (fallback mode).
     */
    public List<double[]> getRouteCoordinates(double startLat, double startLng, double endLat, double endLng) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ OpenRouteService API Key is NOT CONFIGURED");
            logger.error("❌ Please set 'openrouteservice.api.key' in application.properties");
            logger.error("❌ Get your FREE API key at: https://openrouteservice.org/dev/#/signup");
            logger.error("════════════════════════════════════════════════════════════════");
            return Collections.emptyList();
        }

        // Construct URL: API expects longitude,latitude order
        // Equivalent to Python's ors_client.directions(..., geometry=True)
        // geometry=true: Include full geometry (like Python library default)
        // geometry_simplify=false: Don't simplify - get ALL curve points (high resolution)
        String url = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f&geometry=true&geometry_simplify=false",
                BASE_URL, apiKey, startLng, startLat, endLng, endLat);

        logger.info("🌐 Requesting route from OpenRouteService API");
        logger.debug("   Start: ({}, {}) -> End: ({}, {})", startLat, startLng, endLat, endLng);
        logger.debug("   URL: {} (api_key hidden)", BASE_URL);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.error("❌ OpenRouteService returned EMPTY response");
                return Collections.emptyList();
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Navigate to features -> 0 -> geometry -> coordinates (GeoJSON LineString)
            JsonNode featuresNode = rootNode.path("features");
            if (featuresNode.isMissingNode() || !featuresNode.isArray() || featuresNode.isEmpty()) {
                logger.error("❌ No 'features' found in OpenRouteService response");
                logger.debug("   Response JSON: {}", jsonResponse.substring(0, Math.min(500, jsonResponse.length())));
                return Collections.emptyList();
            }

            JsonNode geometryNode = featuresNode.get(0).path("geometry");
            if (geometryNode.isMissingNode()) {
                logger.error("❌ No 'geometry' found in OpenRouteService response");
                return Collections.emptyList();
            }

            JsonNode coordinatesNode = geometryNode.path("coordinates");
            if (coordinatesNode.isMissingNode() || !coordinatesNode.isArray()) {
                logger.error("❌ No 'coordinates' array found in OpenRouteService response");
                return Collections.emptyList();
            }

            // Extract ALL coordinates from the geometry. API returns [lng, lat]; swap to [lat, lng]
            List<double[]> result = new ArrayList<>();
            for (JsonNode coordNode : coordinatesNode) {
                if (coordNode.isArray() && coordNode.size() >= 2) {
                    double longitude = coordNode.get(0).asDouble();
                    double latitude = coordNode.get(1).asDouble();
                    result.add(new double[]{latitude, longitude});
                }
            }

            if (result.isEmpty()) {
                logger.error("❌ OpenRouteService response coordinates list is EMPTY after parsing");
                return Collections.emptyList();
            }

            logger.info("✅ Successfully retrieved {} coordinate points from OpenRouteService (full street geometry)", result.size());

            // Densify route to add intermediate points every 5 meters
            List<double[]> densifiedRoute = densifyRoute(result, 5.0);
            logger.info("🔧 Route densified: {} original points → {} high-resolution points", result.size(), densifiedRoute.size());

            return densifiedRoute;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ HTTP ERROR calling OpenRouteService API");
            logger.error("   Status Code: {} - {}", e.getStatusCode().value(), e.getStatusCode());
            logger.error("   Response Body: {}", e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 401) {
                logger.error("   ════════════════════════════════════════════════");
                logger.error("   🔑 ERROR 401 UNAUTHORIZED");
                logger.error("   Your API Key is INVALID or MISSING");
                logger.error("   Current API Key: {}***", apiKey != null && apiKey.length() > 5 ? apiKey.substring(0, 5) : "null");
                logger.error("   ════════════════════════════════════════════════");
                logger.error("   📋 SOLUTION:");
                logger.error("   1. Go to: https://openrouteservice.org/dev/#/signup");
                logger.error("   2. Sign up and get your FREE API key");
                logger.error("   3. Add to application.properties:");
                logger.error("      openrouteservice.api.key=YOUR_API_KEY_HERE");
                logger.error("   ════════════════════════════════════════════════");
            } else if (e.getStatusCode().value() == 403) {
                logger.error("   🚫 ERROR 403 FORBIDDEN");
                logger.error("   Your API Key doesn't have permission or daily quota exceeded");
                logger.error("   Check your account at: https://openrouteservice.org/dev/#/home");
            } else if (e.getStatusCode().value() == 404) {
                logger.error("   🗺️  ERROR 404 NOT FOUND");
                logger.error("   Route not found between the specified coordinates");
            } else if (e.getStatusCode().value() == 429) {
                logger.error("   ⏱️  ERROR 429 TOO MANY REQUESTS");
                logger.error("   Rate limit exceeded. Free tier: 40 requests/minute, 2000 requests/day");
            }

            logger.error("════════════════════════════════════════════════════════════════");
            return Collections.emptyList();

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ NETWORK ERROR calling OpenRouteService API");
            logger.error("   Error: {}", e.getMessage());
            logger.error("   Cannot reach: {}", BASE_URL);
            logger.error("   Check your internet connection");
            logger.error("════════════════════════════════════════════════════════════════");
            return Collections.emptyList();

        } catch (Exception e) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ UNEXPECTED ERROR calling OpenRouteService API", e);
            logger.error("════════════════════════════════════════════════════════════════");
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves complete route information from OpenRouteService API including
     * all coordinates (following streets), distance, and duration.
     */
    public RouteResponse getCompleteRoute(double startLat, double startLng, double endLat, double endLng) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ OpenRouteService API Key is NOT CONFIGURED");
            logger.error("❌ Please set 'openrouteservice.api.key' in application.properties");
            logger.error("════════════════════════════════════════════════════════════════");
            return null;
        }

        // Equivalent to Python's ors_client.directions(..., geometry=True)
        // geometry=true: Include full geometry
        // geometry_simplify=false: Maximum detail for street curves
        String url = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f&geometry=true&geometry_simplify=false",
                BASE_URL, apiKey, startLng, startLat, endLng, endLat);

        logger.info("🌐 Requesting COMPLETE route from OpenRouteService API");
        logger.debug("   Start: ({}, {}) -> End: ({}, {})", startLat, startLng, endLat, endLng);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.error("❌ OpenRouteService returned EMPTY response");
                return null;
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode featuresNode = rootNode.path("features");
            if (featuresNode.isMissingNode() || !featuresNode.isArray() || featuresNode.isEmpty()) {
                logger.error("❌ No 'features' found in OpenRouteService response");
                return null;
            }

            JsonNode featureNode = featuresNode.get(0);

            JsonNode geometryNode = featureNode.path("geometry");
            if (geometryNode.isMissingNode()) {
                logger.error("❌ No 'geometry' found in OpenRouteService response");
                return null;
            }

            JsonNode coordinatesNode = geometryNode.path("coordinates");
            if (coordinatesNode.isMissingNode() || !coordinatesNode.isArray()) {
                logger.error("❌ No 'coordinates' array found in OpenRouteService response");
                return null;
            }

            List<double[]> coordinates = new ArrayList<>();
            for (JsonNode coordNode : coordinatesNode) {
                if (coordNode.isArray() && coordNode.size() >= 2) {
                    double longitude = coordNode.get(0).asDouble();
                    double latitude = coordNode.get(1).asDouble();
                    coordinates.add(new double[]{latitude, longitude});
                }
            }

            if (coordinates.isEmpty()) {
                logger.error("❌ OpenRouteService response coordinates list is EMPTY after parsing");
                return null;
            }

            // Densify route to add intermediate points every 5 meters
            List<double[]> densifiedCoordinates = densifyRoute(coordinates, 5.0);
            logger.info("🔧 Complete route densified: {} original points → {} high-resolution points",
                    coordinates.size(), densifiedCoordinates.size());

            JsonNode propertiesNode = featureNode.path("properties");
            JsonNode summaryNode = propertiesNode.path("summary");

            Double distance = null;
            Double duration = null;

            if (!summaryNode.isMissingNode()) {
                JsonNode distanceNode = summaryNode.path("distance");
                if (!distanceNode.isMissingNode()) {
                    distance = distanceNode.asDouble();
                }
                JsonNode durationNode = summaryNode.path("duration");
                if (!durationNode.isMissingNode()) {
                    duration = durationNode.asDouble();
                }
            }

            RouteResponse response = new RouteResponse(densifiedCoordinates, distance, duration);

            logger.info("✅ Successfully retrieved complete route: {} points, {} km, {} min, {} km/h avg speed",
                    coordinates.size(),
                    response.getDistanceKilometers() != null ? String.format("%.2f", response.getDistanceKilometers()) : "N/A",
                    response.getDurationMinutes() != null ? String.format("%.2f", response.getDurationMinutes()) : "N/A",
                    response.getAverageSpeedKmh() != null ? String.format("%.2f", response.getAverageSpeedKmh()) : "N/A");

            return response;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ HTTP ERROR calling OpenRouteService API");
            logger.error("   Status Code: {} - {}", e.getStatusCode().value(), e.getStatusCode());
            logger.error("   Response Body: {}", e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 401) {
                logger.error("   🔑 ERROR 401 UNAUTHORIZED - Invalid API Key");
            } else if (e.getStatusCode().value() == 403) {
                logger.error("   🚫 ERROR 403 FORBIDDEN - Permission denied or quota exceeded");
            } else if (e.getStatusCode().value() == 429) {
                logger.error("   ⏱️  ERROR 429 TOO MANY REQUESTS - Rate limit exceeded");
            }

            logger.error("════════════════════════════════════════════════════════════════");
            return null;

        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ NETWORK ERROR calling OpenRouteService API");
            logger.error("   Error: {}", e.getMessage());
            logger.error("════════════════════════════════════════════════════════════════");
            return null;

        } catch (Exception e) {
            logger.error("════════════════════════════════════════════════════════════════");
            logger.error("❌ UNEXPECTED ERROR calling OpenRouteService API", e);
            logger.error("════════════════════════════════════════════════════════════════");
            return null;
        }
    }

    /**
     * Validates if the API client is properly configured.
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    /**
     * Densifies a route by adding intermediate points between waypoints when the distance
     * between consecutive points exceeds the specified threshold.
     *
     * This ensures smooth animations and accurate street-following visualization by preventing
     * straight lines that cut through curves and buildings.
     *
     * @param waypoints Original list of waypoints from the API
     * @param metersBetweenPoints Maximum distance allowed between consecutive points (e.g., 5.0 meters)
     * @return Densified route with additional intermediate points
     */
    private List<double[]> densifyRoute(List<double[]> waypoints, double metersBetweenPoints) {
        if (waypoints == null || waypoints.size() < 2) {
            return waypoints != null ? waypoints : Collections.emptyList();
        }

        List<double[]> densifiedRoute = new ArrayList<>();

        // Always add the first point
        densifiedRoute.add(waypoints.get(0));

        // Process each segment
        for (int i = 0; i < waypoints.size() - 1; i++) {
            double[] pointA = waypoints.get(i);
            double[] pointB = waypoints.get(i + 1);

            double lat1 = pointA[0];
            double lng1 = pointA[1];
            double lat2 = pointB[0];
            double lng2 = pointB[1];

            // Calculate distance between consecutive points using Haversine formula
            double distanceMeters = calculateDistanceHaversine(lat1, lng1, lat2, lng2);

            // If distance is greater than threshold, add intermediate points
            if (distanceMeters > metersBetweenPoints) {
                int numberOfIntermediatePoints = (int) Math.ceil(distanceMeters / metersBetweenPoints) - 1;

                // Generate intermediate points using geodesic interpolation
                for (int j = 1; j <= numberOfIntermediatePoints; j++) {
                    double fraction = (double) j / (numberOfIntermediatePoints + 1);
                    double[] intermediatePoint = interpolateGeodesic(lat1, lng1, lat2, lng2, fraction);
                    densifiedRoute.add(intermediatePoint);
                }
            }

            // Add the end point of the segment (becomes start of next segment)
            densifiedRoute.add(pointB);
        }

        return densifiedRoute;
    }

    /**
     * Calculates the distance between two geographic coordinates using the Haversine formula.
     * This accounts for the Earth's curvature and provides accurate distance in meters.
     *
     * @param lat1 Latitude of first point (degrees)
     * @param lng1 Longitude of first point (degrees)
     * @param lat2 Latitude of second point (degrees)
     * @param lng2 Longitude of second point (degrees)
     * @return Distance in meters
     */
    private double calculateDistanceHaversine(double lat1, double lng1, double lat2, double lng2) {
        final double EARTH_RADIUS_METERS = 6371000.0; // Earth's mean radius in meters

        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLatRad = Math.toRadians(lat2 - lat1);
        double deltaLngRad = Math.toRadians(lng2 - lng1);

        // Haversine formula
        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLngRad / 2) * Math.sin(deltaLngRad / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Interpolates a point on the geodesic (great circle) between two geographic coordinates.
     * Uses spherical linear interpolation (SLERP) for accurate positioning on Earth's surface.
     *
     * This method is more accurate than simple linear interpolation (LERP) for geographic coordinates,
     * as it accounts for the Earth's curvature.
     *
     * @param lat1 Starting point latitude (degrees)
     * @param lng1 Starting point longitude (degrees)
     * @param lat2 Ending point latitude (degrees)
     * @param lng2 Ending point longitude (degrees)
     * @param fraction Position along the path (0.0 = start, 1.0 = end)
     * @return Interpolated coordinate [latitude, longitude]
     */
    private double[] interpolateGeodesic(double lat1, double lng1, double lat2, double lng2, double fraction) {
        // Convert to radians
        double lat1Rad = Math.toRadians(lat1);
        double lng1Rad = Math.toRadians(lng1);
        double lat2Rad = Math.toRadians(lat2);
        double lng2Rad = Math.toRadians(lng2);

        // Calculate angular distance
        double deltaLng = lng2Rad - lng1Rad;

        double a = Math.sin(lat1Rad) * Math.sin(lat2Rad);
        double b = Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLng);
        double d = Math.acos(Math.max(-1.0, Math.min(1.0, a + b))); // Clamp to avoid numerical errors

        // Handle case where points are very close (avoid division by zero)
        if (d < 1e-10) {
            return new double[]{lat1, lng1};
        }

        // Spherical linear interpolation (SLERP)
        double A = Math.sin((1 - fraction) * d) / Math.sin(d);
        double B = Math.sin(fraction * d) / Math.sin(d);

        double x = A * Math.cos(lat1Rad) * Math.cos(lng1Rad) + B * Math.cos(lat2Rad) * Math.cos(lng2Rad);
        double y = A * Math.cos(lat1Rad) * Math.sin(lng1Rad) + B * Math.cos(lat2Rad) * Math.sin(lng2Rad);
        double z = A * Math.sin(lat1Rad) + B * Math.sin(lat2Rad);

        // Convert back to lat/lng
        double latResult = Math.atan2(z, Math.sqrt(x * x + y * y));
        double lngResult = Math.atan2(y, x);

        return new double[]{Math.toDegrees(latResult), Math.toDegrees(lngResult)};
    }
}
