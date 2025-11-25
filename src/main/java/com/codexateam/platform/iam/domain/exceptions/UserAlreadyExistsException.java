package com.codexateam.platform.iam.domain.exceptions;

/**
 * Exception thrown when attempting to create a user with an email that already exists in the system.
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists.");
    }
}

