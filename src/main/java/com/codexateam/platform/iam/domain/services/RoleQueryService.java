package com.codexateam.platform.iam.domain.services;

import com.codexateam.platform.iam.domain.model.entities.Role;
import com.codexateam.platform.iam.domain.model.queries.GetRoleByNameQuery;
import java.util.Optional;

/**
 * Service interface for handling Role queries.
 */
public interface RoleQueryService {
    /**
     * Retrieves a role by its name.
     * @param query The query containing the role name
     * @return An Optional containing the role if found, empty otherwise
     */
    Optional<Role> handle(GetRoleByNameQuery query);
}
