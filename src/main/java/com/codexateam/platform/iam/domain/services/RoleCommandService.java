package com.codexateam.platform.iam.domain.services;

import com.codexateam.platform.iam.domain.model.commands.SeedRolesCommand;

/**
 * Service interface for handling Role commands.
 */
public interface RoleCommandService {
    /**
     * Handles the SeedRolesCommand to initialize system roles.
     * @param command The seed roles command
     */
    void handle(SeedRolesCommand command);
}
