package com.codexateam.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the CodexaTeam Backend application.
 * Enables JPA Auditing to automatically manage CreatedAt and UpdatedAt fields.
 * Enables Async support for asynchronous method execution.
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class CodexaTeamBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodexaTeamBackendApplication.class, args);
    }

}
