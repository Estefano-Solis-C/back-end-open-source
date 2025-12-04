package com.codexateam.platform.shared.domain.exceptions;

/**
 * Base class for all domain-specific exceptions in the application.
 * This class extends RuntimeException to allow unchecked exceptions.
 */
public abstract class DomainException extends RuntimeException {

    /** Constructs a new DomainException with the specified detail message.
     * @param message The detail message
     */
    protected DomainException(String message) {
        super(message);
    }

    /** Constructs a new DomainException with the specified detail message and cause.
     * @param message The detail message
     * @param cause The cause of the exception
     */
    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}

