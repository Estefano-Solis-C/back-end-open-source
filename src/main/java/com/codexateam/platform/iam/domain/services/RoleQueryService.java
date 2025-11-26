package com.codexateam.platform.iam.domain.services;

import com.codexateam.platform.iam.domain.model.entities.Role;
import com.codexateam.platform.iam.domain.model.queries.GetAllRolesQuery;
import com.codexateam.platform.iam.domain.model.queries.GetRoleByNameQuery;
import java.util.List;
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

    /**
     * Retrieves all available roles in the system.
     * @param query The query to get all roles
     * @return A list of all roles
     */
    List<Role> handle(GetAllRolesQuery query);
}
