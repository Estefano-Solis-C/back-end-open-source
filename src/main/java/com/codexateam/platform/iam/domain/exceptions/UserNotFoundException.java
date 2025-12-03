package com.codexateam.platform.iam.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Exception thrown when a user cannot be found by ID or email.
 */
public class UserNotFoundException extends DomainException {
    /**
     * Constructs a new UserNotFoundException with the user ID.
     * @param userId The ID of the user that was not found
     */
    public UserNotFoundException(Long userId) {
        super("User with ID " + userId + " not found.");
    }

    /**
     * Constructs a new UserNotFoundException with the user email.
     * @param email The email of the user that was not found
     */
    public UserNotFoundException(String email) {
        super("User with email " + email + " not found.");
    }
}

