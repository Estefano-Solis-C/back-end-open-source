package com.codexateam.platform.iam.interfaces.rest.transform;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.interfaces.rest.resources.UserResource;

import java.util.stream.Collectors;

/**
 * Assembler to convert User entity to UserResource DTO.
 */
public class UserResourceFromEntityAssembler {
    public static UserResource toResourceFromEntity(User user) {
        var roles = user.getRoles().stream()
                .map(role -> role.getStringName())
                .collect(Collectors.toSet());
        return new UserResource(user.getId(), user.getName(), user.getEmailAddress().value(), roles);
    }
}
