package com.codexateam.platform.iam.application.internal.commandservices;

import com.codexateam.platform.iam.application.internal.outboundservices.hashing.HashingService;
import com.codexateam.platform.iam.domain.exceptions.InvalidPasswordException;
import com.codexateam.platform.iam.domain.exceptions.UserAlreadyExistsException;
import com.codexateam.platform.iam.domain.exceptions.UserNotFoundException;
import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.commands.SignInCommand;
import com.codexateam.platform.iam.domain.model.commands.SignUpCommand;
import com.codexateam.platform.iam.domain.model.commands.UpdatePasswordCommand;
import com.codexateam.platform.iam.domain.model.commands.UpdateUserCommand;
import com.codexateam.platform.iam.domain.model.commands.DeleteUserCommand;
import com.codexateam.platform.iam.domain.model.valueobjects.EmailAddress;
import com.codexateam.platform.iam.domain.services.UserCommandService;
import com.codexateam.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Implementation of UserCommandService.
 * Handles the logic for user sign-up, sign-in, and updates.
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {
    private final UserRepository userRepository;
    private final HashingService hashingService;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
    }

    /**
     * Handles the SignUpCommand.
     * Validates if the email already exists, hashes the password, and saves the new user.
     */
    @Override
    public Optional<User> handle(SignUpCommand command) {
        var emailAddress = new EmailAddress(command.email());

        if (userRepository.existsByEmail(emailAddress)) {
            throw new UserAlreadyExistsException(command.email());
        }
        var user = new User(command.name(), command.email(), hashingService.encode(command.password()), command.roles());
        userRepository.save(user);
        return Optional.of(user);
    }

    /**
     * Handles the SignInCommand.
     * Finds the user by email and validates the password.
     */
    @Override
    public Optional<User> handle(SignInCommand command) {
        var emailAddress = new EmailAddress(command.email());
        var user = userRepository.findByEmail(emailAddress);
        if (user.isEmpty()) {
            throw new UserNotFoundException(command.email());
        }
        if (!hashingService.matches(command.password(), user.get().getPassword())) {
            throw new InvalidPasswordException();
        }
        return user;
    }

    /**
     * Handles updating user profile (name, email).
     * @param command The command containing user ID and new profile data
     * @return An Optional containing the updated user
     */
    @Override
    @Transactional
    public Optional<User> handle(UpdateUserCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(command.userId());
        }
        var user = userOpt.get();
        if (command.name() != null && !command.name().isBlank()) user.setName(command.name());
        if (command.email() != null && !command.email().isBlank()) user.setEmail(command.email());
        userRepository.save(user);
        return Optional.of(user);
    }

    /**
     * Handles updating user password (verify current, then hash new password)
     */
    @Override
    @Transactional
    public Optional<User> handle(UpdatePasswordCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(command.userId());
        }
        var user = userOpt.get();
        if (command.newPassword() == null || command.newPassword().isBlank()) {
            throw new InvalidPasswordException("New password cannot be empty");
        }
        if (command.currentPassword() == null || !hashingService.matches(command.currentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password is incorrect");
        }
        user.setPassword(hashingService.encode(command.newPassword()));
        userRepository.save(user);
        return Optional.of(user);
    }

    /**
     * Handles deleting a user.
     * @param command The command containing the user ID to delete
     */
    @Override
    @Transactional
    public void handle(DeleteUserCommand command) {
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFoundException(command.userId());
        }
        userRepository.deleteById(command.userId());
    }
}
