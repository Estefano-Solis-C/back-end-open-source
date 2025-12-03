package com.codexateam.platform.shared.domain.exceptions;

/**
 * Exception thrown when business validation rules are violated.
 * This is for domain-level validations beyond simple null checks.
 */
public class ValidationException extends DomainException {

    /**
     * Constructs a new ValidationException with the specified detail message.
     * @param message The detail message explaining the validation failure
     */
    public ValidationException(String message) {
        super(message);
    }
}

