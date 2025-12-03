package com.codexateam.platform.iam.domain.exceptions;

import com.codexateam.platform.shared.domain.exceptions.DomainException;

/**
 * Exception thrown when attempting to create a user with an email that already exists in the system.
 */
public class UserAlreadyExistsException extends DomainException {
    public UserAlreadyExistsException(String email) {
        super("User with email " + email + " already exists.");
    }
}


