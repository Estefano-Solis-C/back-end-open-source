package com.codexateam.platform.shared.domain.exceptions;

/**
 * Exception thrown when a user attempts to perform an action they are not authorized to execute.
 * This represents authorization failures (user is authenticated but lacks permissions).
 */
public class UnauthorizedAccessException extends DomainException {

    /**
     * Constructs a new UnauthorizedAccessException with the specified detail message.
     * @param message The detail message explaining the authorization failure
     */
    public UnauthorizedAccessException(String message) {
        super(message);
    }
}

