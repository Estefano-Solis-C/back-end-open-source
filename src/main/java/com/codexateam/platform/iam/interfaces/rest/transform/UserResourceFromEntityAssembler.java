package com.codexateam.platform.iam.interfaces.rest.transform;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.entities.Role;
import com.codexateam.platform.iam.interfaces.rest.resources.UserResource;

import java.util.stream.Collectors;

/**
 * Assembler to convert User entity to UserResource DTO.
 */
public class UserResourceFromEntityAssembler {
    /**
     * Converts a User entity to a UserResource DTO.
     * @param user The User entity to convert
     * @return The UserResource DTO
     */
    public static UserResource toResourceFromEntity(User user) {
        var roles = user.getRoles().stream()
                .map(Role::getStringName)
                .collect(Collectors.toSet());
        return new UserResource(user.getId(), user.getName(), user.getEmailAddress().value(), roles);
    }
}
