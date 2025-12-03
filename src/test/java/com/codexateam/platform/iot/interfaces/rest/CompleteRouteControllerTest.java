package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iot.domain.exceptions.RouteNotFoundException;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.infrastructure.external.dto.RouteResponse;
import com.codexateam.platform.iot.interfaces.rest.resources.CompleteRouteResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the new CompleteRoute endpoint in RouteController.
 */
@ExtendWith(MockitoExtension.class)
class CompleteRouteControllerTest {

    @Mock
    private OpenRouteServiceApiClient openRouteServiceApiClient;

    @InjectMocks
    private RouteController routeController;

    private Double validStartLat;
    private Double validStartLng;
    private Double validEndLat;
    private Double validEndLng;
    private RouteResponse mockRouteResponse;

    @BeforeEach
    void setUp() {
        validStartLat = -12.046374;
        validStartLng = -77.042793;
        validEndLat = -12.056189;
        validEndLng = -77.029317;

        // Setup mock route response with realistic data
        List<double[]> mockCoordinates = new ArrayList<>();
        mockCoordinates.add(new double[]{-12.046374, -77.042793});
        mockCoordinates.add(new double[]{-12.046812, -77.042456});
        mockCoordinates.add(new double[]{-12.047231, -77.042123});
        mockCoordinates.add(new double[]{-12.047650, -77.041790});
        mockCoordinates.add(new double[]{-12.056189, -77.029317});

        // Distance: 2500 meters, Duration: 420 seconds (7 minutes)
        mockRouteResponse = new RouteResponse(mockCoordinates, 2500.0, 420.0);
    }

    @Test
    void getCompleteRoute_WithValidCoordinates_ShouldReturnCompleteRouteInfo() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(mockRouteResponse);

        // Act
        ResponseEntity<CompleteRouteResource> response = routeController.getCompleteRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        CompleteRouteResource resource = response.getBody();
        assertEquals(5, resource.coordinates().size());
        assertEquals(2500.0, resource.distanceMeters());
        assertEquals(420.0, resource.durationSeconds());
        assertEquals(2.5, resource.distanceKm());
        assertEquals(7.0, resource.durationMinutes());
        assertNotNull(resource.averageSpeedKmh());
        assertTrue(resource.averageSpeedKmh() > 0);

        // Verify coordinates format
        List<Double> firstCoord = resource.coordinates().get(0);
        assertEquals(-12.046374, firstCoord.get(0));
        assertEquals(-77.042793, firstCoord.get(1));

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, times(1)).getCompleteRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );
    }

    @Test
    void getCompleteRoute_WithInvalidCoordinates_ShouldThrowException() {
        // Arrange
        Double invalidLat = 100.0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            routeController.getCompleteRoute(invalidLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, never()).isConfigured();
        verify(openRouteServiceApiClient, never()).getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    void getCompleteRoute_WithServiceNotConfigured_ShouldThrowRouteNotFoundException() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(false);

        // Act & Assert
        assertThrows(RouteNotFoundException.class, () -> {
            routeController.getCompleteRoute(validStartLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, never()).getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    void getCompleteRoute_WithNullResponse_ShouldThrowRouteNotFoundException() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(null);

        // Act & Assert
        assertThrows(RouteNotFoundException.class, () -> {
            routeController.getCompleteRoute(validStartLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, times(1)).getCompleteRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );
    }

    @Test
    void getCompleteRoute_WithEmptyCoordinates_ShouldThrowRouteNotFoundException() {
        // Arrange
        RouteResponse emptyResponse = new RouteResponse(new ArrayList<>(), 0.0, 0.0);
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(emptyResponse);

        // Act & Assert
        assertThrows(RouteNotFoundException.class, () -> {
            routeController.getCompleteRoute(validStartLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, times(1)).getCompleteRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );
    }

    @Test
    void getCompleteRoute_VerifyAverageSpeedCalculation() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(mockRouteResponse);

        // Act
        ResponseEntity<CompleteRouteResource> response = routeController.getCompleteRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response.getBody());
        Double avgSpeed = response.getBody().averageSpeedKmh();
        assertNotNull(avgSpeed);

        // Distance: 2.5 km, Duration: 7 minutes (0.1167 hours)
        // Expected speed: 2.5 / 0.1167 ≈ 21.43 km/h
        assertTrue(avgSpeed > 20 && avgSpeed < 22);
    }

    @Test
    void getCompleteRoute_WithLongRoute_ShouldHandleCorrectly() {
        // Arrange
        List<double[]> longRoute = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            longRoute.add(new double[]{-12.046374 + i * 0.001, -77.042793 + i * 0.001});
        }
        RouteResponse longRouteResponse = new RouteResponse(longRoute, 15000.0, 1800.0);

        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getCompleteRoute(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(longRouteResponse);

        // Act
        ResponseEntity<CompleteRouteResource> response = routeController.getCompleteRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response.getBody());
        assertEquals(100, response.getBody().coordinates().size());
        assertEquals(15.0, response.getBody().distanceKm());
        assertEquals(30.0, response.getBody().durationMinutes());
    }
}

