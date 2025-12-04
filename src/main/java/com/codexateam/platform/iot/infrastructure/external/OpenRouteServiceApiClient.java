package com.codexateam.platform.iot.infrastructure.external;

import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class OpenRouteServiceApiClient {

    private static final String BASE_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    @Value("${openrouteservice.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenRouteServiceApiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Gets route coordinates between two points.
     *
     * @param startLat Starting latitude
     * @param startLng Starting longitude
     * @param endLat Ending latitude
     * @param endLng Ending longitude
     * @return List of coordinate points forming the route
     */
    public List<double[]> getRouteCoordinates(double startLat, double startLng, double endLat, double endLng) {
        if (!isConfigured()) {
            return Collections.emptyList();
        }

        String url = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f&geometry=true",
                BASE_URL, apiKey, startLng, startLat, endLng, endLat);

        try {
            String jsonResponse = restTemplate.getForObject(url, String.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                return Collections.emptyList();
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode featuresNode = rootNode.path("features");

            if (featuresNode.isMissingNode() || featuresNode.isEmpty()) {
                return Collections.emptyList();
            }

            JsonNode geometryNode = featuresNode.get(0).path("geometry");
            JsonNode coordinatesNode = geometryNode.path("coordinates");

            List<double[]> result = new ArrayList<>();
            if (coordinatesNode.isArray()) {
                for (JsonNode coordNode : coordinatesNode) {
                    double lng = coordNode.get(0).asDouble();
                    double lat = coordNode.get(1).asDouble();
                    result.add(new double[]{lat, lng});
                }
            }

            return result;

        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public RouteResponse getCompleteRoute(double startLat, double startLng, double endLat, double endLng) {
        List<double[]> coords = getRouteCoordinates(startLat, startLng, endLat, endLng);
        if (coords.isEmpty()) return null;

        return new RouteResponse(coords, 0.0, 0.0);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}