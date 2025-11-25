package com.codexateam.platform.iam.domain.exceptions;

/**
 * Exception thrown when password validation fails.
 * This can occur during sign-in (incorrect password) or password updates (invalid current password).
 */
public class InvalidPasswordException extends RuntimeException {
    public InvalidPasswordException() {
        super("Invalid password.");
    }

    public InvalidPasswordException(String message) {
        super(message);
    }
}

