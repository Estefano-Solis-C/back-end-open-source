package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import java.util.Optional;

/**
 * Anti-Corruption Layer (ACL) facade for accessing IAM bounded context from IoT.
 * Provides methods to retrieve user information.
 */
public interface ExternalIamService {

    /**
     * Gets the full name of a user by their ID.
     *
     * @param userId The ID of the user.
     * @return The user's full name wrapped in Optional, or empty if user not found.
     */
    Optional<String> getUserFullName(Long userId);
}

