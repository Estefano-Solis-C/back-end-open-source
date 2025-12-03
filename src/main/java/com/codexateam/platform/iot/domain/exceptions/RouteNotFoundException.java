package com.codexateam.platform.iot.domain.exceptions;

/**
 * Exception thrown when a route cannot be retrieved from the external routing service.
 * This may occur due to invalid coordinates, network issues, or service unavailability.
 */
public class RouteNotFoundException extends RuntimeException {

    /**
     * Constructs a new RouteNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public RouteNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a new RouteNotFoundException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public RouteNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

