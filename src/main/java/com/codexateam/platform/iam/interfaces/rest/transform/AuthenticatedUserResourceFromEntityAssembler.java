package com.codexateam.platform.iam.interfaces.rest.transform;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.interfaces.rest.resources.AuthenticatedUserResource;

import java.util.stream.Collectors;

/**
 * Assembler to convert User entity to AuthenticatedUserResource DTO.
 */
public class AuthenticatedUserResourceFromEntityAssembler {
    /**
     * Converts a User entity and JWT token to an AuthenticatedUserResource DTO.
     * @param user The User entity to convert
     * @param token The JWT authentication token
     * @return The AuthenticatedUserResource DTO with user details and token
     */
    public static AuthenticatedUserResource toResourceFromEntity(User user, String token) {
        var roles = user.getRoles().stream()
                .map(role -> role.getStringName())
                .collect(Collectors.toSet());
        return new AuthenticatedUserResource(user.getId(), user.getEmailAddress().value(), user.getName(), roles, token);
    }
}