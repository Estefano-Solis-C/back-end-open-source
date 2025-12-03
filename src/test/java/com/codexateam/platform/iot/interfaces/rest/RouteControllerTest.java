package com.codexateam.platform.iot.interfaces.rest;

import com.codexateam.platform.iot.domain.exceptions.RouteNotFoundException;
import com.codexateam.platform.iot.infrastructure.external.OpenRouteServiceApiClient;
import com.codexateam.platform.iot.interfaces.rest.resources.RouteCoordinateResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RouteController.
 * Tests all endpoint functionality including validation and error handling.
 */
@ExtendWith(MockitoExtension.class)
class RouteControllerTest {

    @Mock
    private OpenRouteServiceApiClient openRouteServiceApiClient;

    @InjectMocks
    private RouteController routeController;

    private Double validStartLat;
    private Double validStartLng;
    private Double validEndLat;
    private Double validEndLng;
    private List<double[]> mockCoordinates;

    @BeforeEach
    void setUp() {
        validStartLat = -12.046374;
        validStartLng = -77.042793;
        validEndLat = -12.056189;
        validEndLng = -77.029317;

        // Setup mock coordinates
        mockCoordinates = new ArrayList<>();
        mockCoordinates.add(new double[]{-12.046374, -77.042793});
        mockCoordinates.add(new double[]{-12.046812, -77.042456});
        mockCoordinates.add(new double[]{-12.056189, -77.029317});
    }

    @Test
    void getRoute_WithValidCoordinates_ShouldReturnRouteCoordinates() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(mockCoordinates);

        // Act
        ResponseEntity<List<RouteCoordinateResource>> response = routeController.getRoute(
                validStartLat, validStartLng, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());

        RouteCoordinateResource firstCoord = response.getBody().get(0);
        assertEquals(-12.046374, firstCoord.lat());
        assertEquals(-77.042793, firstCoord.lng());

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, times(1)).getRouteCoordinates(
                validStartLat, validStartLng, validEndLat, validEndLng
        );
    }

    @Test
    void getRoute_WithInvalidLatitude_ShouldThrowIllegalArgumentException() {
        // Arrange
        Double invalidLat = 100.0; // Latitude out of range

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            routeController.getRoute(invalidLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, never()).isConfigured();
        verify(openRouteServiceApiClient, never()).getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    void getRoute_WithInvalidLongitude_ShouldThrowIllegalArgumentException() {
        // Arrange
        Double invalidLng = 200.0; // Longitude out of range

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            routeController.getRoute(validStartLat, invalidLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, never()).isConfigured();
        verify(openRouteServiceApiClient, never()).getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    void getRoute_WithNullCoordinates_ShouldThrowIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            routeController.getRoute(null, validStartLng, validEndLat, validEndLng);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            routeController.getRoute(validStartLat, null, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, never()).isConfigured();
        verify(openRouteServiceApiClient, never()).getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    void getRoute_WithServiceNotConfigured_ShouldThrowRouteNotFoundException() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(false);

        // Act & Assert
        assertThrows(RouteNotFoundException.class, () -> {
            routeController.getRoute(validStartLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, never()).getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        );
    }

    @Test
    void getRoute_WithEmptyCoordinatesResponse_ShouldThrowRouteNotFoundException() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(RouteNotFoundException.class, () -> {
            routeController.getRoute(validStartLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, times(1)).getRouteCoordinates(
                validStartLat, validStartLng, validEndLat, validEndLng
        );
    }

    @Test
    void getRoute_WithNullCoordinatesResponse_ShouldThrowRouteNotFoundException() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(null);

        // Act & Assert
        assertThrows(RouteNotFoundException.class, () -> {
            routeController.getRoute(validStartLat, validStartLng, validEndLat, validEndLng);
        });

        verify(openRouteServiceApiClient, times(1)).isConfigured();
        verify(openRouteServiceApiClient, times(1)).getRouteCoordinates(
                validStartLat, validStartLng, validEndLat, validEndLng
        );
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid coordinates");

        // Act
        ResponseEntity<String> response = routeController.handleIllegalArgumentException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid coordinates", response.getBody());
    }

    @Test
    void handleRouteNotFoundException_ShouldReturnNotFound() {
        // Arrange
        RouteNotFoundException exception = new RouteNotFoundException("Route not found");

        // Act
        ResponseEntity<String> response = routeController.handleRouteNotFoundException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Route not found", response.getBody());
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<String> response = routeController.handleGenericException(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertTrue(response.getBody().contains("unexpected error"));
    }

    @Test
    void getRoute_WithBoundaryLatitudeValues_ShouldWork() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(mockCoordinates);

        // Act - Test with -90 (minimum latitude)
        ResponseEntity<List<RouteCoordinateResource>> response1 = routeController.getRoute(
                -90.0, validStartLng, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        // Act - Test with 90 (maximum latitude)
        ResponseEntity<List<RouteCoordinateResource>> response2 = routeController.getRoute(
                90.0, validStartLng, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }

    @Test
    void getRoute_WithBoundaryLongitudeValues_ShouldWork() {
        // Arrange
        when(openRouteServiceApiClient.isConfigured()).thenReturn(true);
        when(openRouteServiceApiClient.getRouteCoordinates(
                anyDouble(), anyDouble(), anyDouble(), anyDouble()
        )).thenReturn(mockCoordinates);

        // Act - Test with -180 (minimum longitude)
        ResponseEntity<List<RouteCoordinateResource>> response1 = routeController.getRoute(
                validStartLat, -180.0, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response1);
        assertEquals(HttpStatus.OK, response1.getStatusCode());

        // Act - Test with 180 (maximum longitude)
        ResponseEntity<List<RouteCoordinateResource>> response2 = routeController.getRoute(
                validStartLat, 180.0, validEndLat, validEndLng
        );

        // Assert
        assertNotNull(response2);
        assertEquals(HttpStatus.OK, response2.getStatusCode());
    }
}

