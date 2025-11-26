package com.codexateam.platform.iam.interfaces.rest.transform;

import com.codexateam.platform.iam.domain.model.commands.UpdateUserCommand;
import com.codexateam.platform.iam.interfaces.rest.resources.UpdateUserResource;

/**
 * Assembler to convert UpdateUserResource to UpdateUserCommand.
 */
public class UpdateUserCommandFromResourceAssembler {
    /**
     * Converts an UpdateUserResource DTO to an UpdateUserCommand.
     * @param userId The ID of the user to update
     * @param resource The update user resource DTO
     * @return The UpdateUserCommand
     */
    public static UpdateUserCommand toCommandFromResource(Long userId, UpdateUserResource resource) {
        return new UpdateUserCommand(userId, resource.name(), resource.email());
    }
}

