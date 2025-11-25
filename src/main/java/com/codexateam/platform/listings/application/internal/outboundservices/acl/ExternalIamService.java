package com.codexateam.platform.listings.application.internal.outboundservices.acl;

/**
 * Anti-Corruption Layer (ACL) facade for accessing IAM bounded context from Listings.
 * Provides methods to validate user roles and permissions for vehicle ownership.
 */
public interface ExternalIamService {

    /**
     * Checks if a user exists and has the ROLE_ARRENDADOR role.
     * This validates that the user is authorized to create and manage vehicle listings.
     *
     * @param userId The ID of the user to validate.
     * @return true if the user exists and has ROLE_ARRENDADOR, false otherwise.
     */
    boolean isOwner(Long userId);
}

