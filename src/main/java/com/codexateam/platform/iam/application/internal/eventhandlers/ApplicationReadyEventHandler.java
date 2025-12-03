package com.codexateam.platform.iam.application.internal.eventhandlers;

import com.codexateam.platform.iam.domain.model.commands.SeedRolesCommand;
import com.codexateam.platform.iam.domain.services.RoleCommandService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * Event handler to seed initial data once the application is ready.
 * This handler triggers the RoleCommandService to seed the 'roles' table.
 */
@Service
public class ApplicationReadyEventHandler {
    private final RoleCommandService roleCommandService;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        roleCommandService.handle(new SeedRolesCommand());
    }
}
