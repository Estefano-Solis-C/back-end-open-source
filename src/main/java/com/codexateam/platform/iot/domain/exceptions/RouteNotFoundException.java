package com.codexateam.platform.iot.domain.exceptions;
import com.codexateam.platform.shared.domain.exceptions.ResourceNotFoundException;

/**
 * Exception thrown when a route cannot be retrieved from the external routing service.
 * This may occur due to invalid coordinates, network issues, or service unavailability.
 */
public class RouteNotFoundException extends ResourceNotFoundException {

    /**
     * Constructs a new RouteNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public RouteNotFoundException(String message) {
        super(message);
    }
}


