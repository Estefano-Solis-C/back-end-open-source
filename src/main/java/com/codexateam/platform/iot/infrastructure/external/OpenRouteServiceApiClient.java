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

@Service
public class OpenRouteServiceApiClient {

    private static final Logger logger = LoggerFactory.getLogger(OpenRouteServiceApiClient.class);
    private static final String BASE_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    @Value("${openrouteservice.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public OpenRouteServiceApiClient(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper();
    }

    // Método simplificado para obtener solo las coordenadas (como pediste en el prompt anterior)
    public List<double[]> getRouteCoordinates(double startLat, double startLng, double endLat, double endLng) {
        logger.info("🚗 [BACKEND] Iniciando petición a OpenRouteService...");
        logger.info("   📍 Desde: {}, {} -> Hasta: {}, {}", startLat, startLng, endLat, endLng);

        if (!isConfigured()) {
            logger.error("❌ [BACKEND] API Key no configurada. Retornando lista vacía.");
            return Collections.emptyList();
        }

        // URL limpia con geometry=true para obtener la forma de la ruta
        String url = String.format("%s?api_key=%s&start=%f,%f&end=%f,%f&geometry=true",
                BASE_URL, apiKey, startLng, startLat, endLng, endLat);

        try {
            long startTime = System.currentTimeMillis();
            String jsonResponse = restTemplate.getForObject(url, String.class);
            long duration = System.currentTimeMillis() - startTime;

            logger.info("✅ [BACKEND] Respuesta recibida en {} ms", duration);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                logger.error("❌ [BACKEND] El cuerpo de la respuesta está vacío.");
                return Collections.emptyList();
            }

            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            JsonNode featuresNode = rootNode.path("features");

            if (featuresNode.isMissingNode() || featuresNode.isEmpty()) {
                logger.error("❌ [BACKEND] No se encontraron 'features' en el JSON. Posible error de la API: {}", jsonResponse);
                return Collections.emptyList();
            }

            // Extraer geometría
            JsonNode geometryNode = featuresNode.get(0).path("geometry");
            JsonNode coordinatesNode = geometryNode.path("coordinates");

            List<double[]> result = new ArrayList<>();
            if (coordinatesNode.isArray()) {
                for (JsonNode coordNode : coordinatesNode) {
                    // ORS devuelve [longitud, latitud], invertimos a [latitud, longitud]
                    double lng = coordNode.get(0).asDouble();
                    double lat = coordNode.get(1).asDouble();
                    result.add(new double[]{lat, lng});
                }
            }

            logger.info("✨ [BACKEND] Ruta decodificada con éxito: {} puntos encontrados (curvas y calles).", result.size());
            return result;

        } catch (Exception e) {
            logger.error("🔥 [BACKEND] Excepción al llamar a OpenRouteService: {}", e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public RouteResponse getCompleteRoute(double startLat, double startLng, double endLat, double endLng) {
        // Reutilizamos la lógica de coordenadas para mantenerlo DRY, o implementamos similar si necesitas métricas
        List<double[]> coords = getRouteCoordinates(startLat, startLng, endLat, endLng);
        if (coords.isEmpty()) return null;

        // Retornamos objeto simple
        return new RouteResponse(coords, 0.0, 0.0);
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }
}