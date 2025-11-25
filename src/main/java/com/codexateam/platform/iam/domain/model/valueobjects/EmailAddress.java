package com.codexateam.platform.iam.domain.model.valueobjects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;

/**
 * Value Object representing an Email Address with basic format validation.
 */
@Embeddable
public record EmailAddress(@Column(name = "email", length = 50, nullable = false, unique = true) String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", Pattern.CASE_INSENSITIVE);

    public EmailAddress {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email address cannot be null or blank");
        }
        if (value.length() > 50) {
            throw new IllegalArgumentException("Email address length must be <= 50 characters");
        }
        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
    }

    @Override
    public String toString() {
        return value;
    }
}

