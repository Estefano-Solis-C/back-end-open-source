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
            logger.error("OpenRouteService API key is not configured. Please set 'openrouteservice.api.key' in application.properties");
            return Collections.emptyList();
        }

        // Construct URL: API expects longitude,latitude order
        // Format: /driving-car?api_key={key}&start={startLng},{startLat}&end={endLng},{endLat}
        String url = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f",
                BASE_URL, apiKey, startLng, startLat, endLng, endLat);

        logger.info("Requesting route from OpenRouteService: start({}, {}) -> end({}, {})",
                startLat, startLng, endLat, endLng);

        try {
            // Use restTemplate.getForObject to get the raw JSON string
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.error("OpenRouteService returned empty response");
                return Collections.emptyList();
            }

            // Parse JSON using ObjectMapper
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Navigate to features -> 0 -> geometry -> coordinates
            JsonNode featuresNode = rootNode.path("features");
            if (featuresNode.isMissingNode() || !featuresNode.isArray() || featuresNode.isEmpty()) {
                logger.warn("No features found in OpenRouteService response");
                return Collections.emptyList();
            }

            JsonNode geometryNode = featuresNode.get(0).path("geometry");
            if (geometryNode.isMissingNode()) {
                logger.error("No geometry found in OpenRouteService response");
                return Collections.emptyList();
            }

            JsonNode coordinatesNode = geometryNode.path("coordinates");
            if (coordinatesNode.isMissingNode() || !coordinatesNode.isArray()) {
                logger.error("No coordinates array found in OpenRouteService response");
                return Collections.emptyList();
            }

            // Extract coordinates into List<double[]>
            // API returns [longitude, latitude] -> swap to [latitude, longitude] for our system
            List<double[]> result = new ArrayList<>();
            for (JsonNode coordNode : coordinatesNode) {
                if (coordNode.isArray() && coordNode.size() >= 2) {
                    double longitude = coordNode.get(0).asDouble();
                    double latitude = coordNode.get(1).asDouble();

                    // Swap to [latitude, longitude] for our system
                    result.add(new double[]{latitude, longitude});
                }
            }

            if (result.isEmpty()) {
                logger.warn("OpenRouteService response coordinates list is empty");
                return Collections.emptyList();
            }

            logger.info("Successfully retrieved {} coordinate points from OpenRouteService", result.size());
            return result;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("HTTP error calling OpenRouteService API ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Collections.emptyList();
        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("Network error calling OpenRouteService API: {}", e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            logger.error("Unexpected error calling OpenRouteService API", e);
            return Collections.emptyList();
        }
    }

    /**
     * Retrieves complete route information from OpenRouteService API including
     * all coordinates (following streets), distance, and duration.
     *
     * This method extracts the complete geometry from the GeoJSON response to ensure
     * the route follows actual streets and roads, not just straight lines.
     *
     * @param startLat Starting point latitude
     * @param startLng Starting point longitude
     * @param endLat Ending point latitude
     * @param endLng Ending point longitude
     * @return RouteResponse containing coordinates, distance, and duration, or null if failed
     */
    public RouteResponse getCompleteRoute(double startLat, double startLng, double endLat, double endLng) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("OpenRouteService API key is not configured. Please set 'openrouteservice.api.key' in application.properties");
            return null;
        }

        // Construct URL: API expects longitude,latitude order
        String url = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f",
                BASE_URL, apiKey, startLng, startLat, endLng, endLat);

        logger.info("Requesting complete route from OpenRouteService: start({}, {}) -> end({}, {})",
                startLat, startLng, endLat, endLng);

        try {
            // Get JSON response from API
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.error("OpenRouteService returned empty response");
                return null;
            }

            // Parse JSON using ObjectMapper
            JsonNode rootNode = objectMapper.readTree(jsonResponse);

            // Navigate to features array
            JsonNode featuresNode = rootNode.path("features");
            if (featuresNode.isMissingNode() || !featuresNode.isArray() || featuresNode.isEmpty()) {
                logger.warn("No features found in OpenRouteService response");
                return null;
            }

            JsonNode featureNode = featuresNode.get(0);

            // Extract geometry coordinates (the complete route following streets)
            JsonNode geometryNode = featureNode.path("geometry");
            if (geometryNode.isMissingNode()) {
                logger.error("No geometry found in OpenRouteService response");
                return null;
            }

            JsonNode coordinatesNode = geometryNode.path("coordinates");
            if (coordinatesNode.isMissingNode() || !coordinatesNode.isArray()) {
                logger.error("No coordinates array found in OpenRouteService response");
                return null;
            }

            // Extract ALL coordinates from the geometry (this is the complete path following streets)
            List<double[]> coordinates = new ArrayList<>();
            for (JsonNode coordNode : coordinatesNode) {
                if (coordNode.isArray() && coordNode.size() >= 2) {
                    double longitude = coordNode.get(0).asDouble();
                    double latitude = coordNode.get(1).asDouble();
                    // Swap to [latitude, longitude] for consistency
                    coordinates.add(new double[]{latitude, longitude});
                }
            }

            if (coordinates.isEmpty()) {
                logger.warn("OpenRouteService response coordinates list is empty");
                return null;
            }

            // Extract summary information (distance and duration)
            JsonNode propertiesNode = featureNode.path("properties");
            JsonNode summaryNode = propertiesNode.path("summary");

            Double distance = null;
            Double duration = null;

            if (!summaryNode.isMissingNode()) {
                // Distance is in meters
                JsonNode distanceNode = summaryNode.path("distance");
                if (!distanceNode.isMissingNode()) {
                    distance = distanceNode.asDouble();
                }

                // Duration is in seconds
                JsonNode durationNode = summaryNode.path("duration");
                if (!durationNode.isMissingNode()) {
                    duration = durationNode.asDouble();
                }
            }

            // Create response object
            RouteResponse response = new RouteResponse(coordinates, distance, duration);

            logger.info("Successfully retrieved complete route: {} points, {} km, {} min, {} km/h avg speed",
                    coordinates.size(),
                    response.getDistanceKilometers() != null ? String.format("%.2f", response.getDistanceKilometers()) : "N/A",
                    response.getDurationMinutes() != null ? String.format("%.2f", response.getDurationMinutes()) : "N/A",
                    response.getAverageSpeedKmh() != null ? String.format("%.2f", response.getAverageSpeedKmh()) : "N/A");

            return response;

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("HTTP error calling OpenRouteService API ({}): {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (org.springframework.web.client.ResourceAccessException e) {
            logger.error("Network error calling OpenRouteService API: {}", e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error("Unexpected error calling OpenRouteService API", e);
            return null;
        }
    }

    /**
     * Validates if the API client is properly configured.
     *
     * @return true if the API key is set, false otherwise
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}
