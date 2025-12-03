package com.codexateam.platform.shared.domain.exceptions;
/**
 * Generic exception thrown when a requested resource is not found.
 * This should be extended by specific domain exceptions for better context.
 */
public class ResourceNotFoundException extends DomainException {
    /**
     * Constructs a new ResourceNotFoundException with the specified detail message.
     * @param message The detail message explaining which resource was not found
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}