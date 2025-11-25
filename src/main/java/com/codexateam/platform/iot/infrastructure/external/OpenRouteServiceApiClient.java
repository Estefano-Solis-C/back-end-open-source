package com.codexateam.platform.iot.infrastructure.external;

import com.codexateam.platform.iot.infrastructure.external.dto.OpenRouteServiceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Client for consuming the OpenRouteService API to retrieve vehicle routing information.
 * This service is used to simulate realistic trips by fetching actual road coordinates
 * between two geographic points.
 */
@Service
public class OpenRouteServiceApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouteServiceApiClient.class);
    private static final String BASE_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    @Value("${openrouteservice.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public OpenRouteServiceApiClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Retrieves route coordinates from OpenRouteService API.
     *
     * @param startLng Starting point longitude
     * @param startLat Starting point latitude
     * @param endLng Ending point longitude
     * @param endLat Ending point latitude
     * @return List of coordinate pairs [longitude, latitude] representing the route.
     *         Returns empty list if the request fails.
     */
    public List<double[]> getRouteCoordinates(double startLat, double startLng, double endLat, double endLng) {
        if (apiKey == null || apiKey.isBlank()) {
            logger.error("OpenRouteService API key is not configured. Please set 'openrouteservice.api.key' in application.properties");
            return Collections.emptyList();
        }

        try {
            // Build the URL with coordinates
            // Format: /driving-car?start=lng,lat&end=lng,lat
            String url = UriComponentsBuilder.fromUriString(BASE_URL)
                    .queryParam("start", String.format("%.6f,%.6f", startLng, startLat))
                    .queryParam("end", String.format("%.6f,%.6f", endLng, endLat))
                    .toUriString();

            logger.info("Requesting route from OpenRouteService: start({}, {}) -> end({}, {})",
                    startLat, startLng, endLat, endLng);

            // Set up headers with API key
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", apiKey);
            headers.set("Accept", "application/json, application/geo+json, application/gpx+xml, img/png; charset=utf-8");

            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            // Make the GET request
            org.springframework.http.ResponseEntity<OpenRouteServiceResponse> response =
                    restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, entity, OpenRouteServiceResponse.class);

            // Parse response and extract coordinates
            if (response.getBody() != null &&
                response.getBody().getRoutes() != null &&
                !response.getBody().getRoutes().isEmpty()) {

                OpenRouteServiceResponse.Route route = response.getBody().getRoutes().getFirst();
                if (route.getGeometry() != null && route.getGeometry().getCoordinates() != null) {
                    List<List<Double>> coordinates = route.getGeometry().getCoordinates();
                    List<double[]> result = new ArrayList<>();

                    for (List<Double> coord : coordinates) {
                        if (coord.size() >= 2) {
                            // OpenRouteService returns [longitude, latitude]
                            result.add(new double[]{coord.get(1), coord.get(0)}); // Convert to [latitude, longitude]
                        }
                    }

                    logger.info("Successfully retrieved {} coordinate points from OpenRouteService", result.size());
                    return result;
                }
            }

            logger.warn("No route data found in OpenRouteService response");
            return Collections.emptyList();

        } catch (org.springframework.web.client.HttpClientErrorException e) {
            logger.error("HTTP error calling OpenRouteService API: {} - {}", e.getStatusCode(), e.getMessage());
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
     * Validates if the API client is properly configured.
     *
     * @return true if the API key is set, false otherwise
     */
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}

