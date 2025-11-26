package com.codexateam.platform.iam.domain.services;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.commands.SignInCommand;
import com.codexateam.platform.iam.domain.model.commands.SignUpCommand;
import com.codexateam.platform.iam.domain.model.commands.UpdatePasswordCommand;
import com.codexateam.platform.iam.domain.model.commands.UpdateUserCommand;
import com.codexateam.platform.iam.domain.model.commands.DeleteUserCommand;

import java.util.Optional;

/**
 * Service interface for handling User commands (SignUp, SignIn, Update...).
 */
public interface UserCommandService {
    /**
     * Handles the SignUpCommand to register a new user.
     * @param command The sign-up command with user details
     * @return An Optional containing the created user if successful
     */
    Optional<User> handle(SignUpCommand command);

    /**
     * Handles the SignInCommand to authenticate a user.
     * @param command The sign-in command with credentials
     * @return An Optional containing the authenticated user if successful
     */
    Optional<User> handle(SignInCommand command);

    /**
     * Handles the UpdateUserCommand to update user profile.
     * @param command The update command with new user data
     * @return An Optional containing the updated user if successful
     */
    Optional<User> handle(UpdateUserCommand command);

    /**
     * Handles the UpdatePasswordCommand to change user password.
     * @param command The command with current and new passwords
     * @return An Optional containing the updated user if successful
     */
    Optional<User> handle(UpdatePasswordCommand command);

    /**
     * Handles the DeleteUserCommand to remove a user.
     * @param command The command containing the user ID to delete
     */
    void handle(DeleteUserCommand command);
}
