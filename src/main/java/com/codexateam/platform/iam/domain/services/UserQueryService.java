package com.codexateam.platform.iam.domain.services;

import java.util.List;
import java.util.Optional;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.queries.GetAllUsersQuery;
import com.codexateam.platform.iam.domain.model.queries.GetUserByIdQuery;

/**
 * Service interface for handling User queries.
 */
public interface UserQueryService {
    /**
     * Retrieves all users in the system.
     * @param query The query to retrieve all users
     * @return List of all users
     */
    List<User> handle(GetAllUsersQuery query);

    /**
     * Retrieves a user by their unique identifier.
     * @param query The query containing the user ID
     * @return An Optional containing the user if found, empty otherwise
     */
    Optional<User> handle(GetUserByIdQuery query);
}
