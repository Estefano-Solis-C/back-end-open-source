package com.codexateam.platform.iam.infrastructure.authorization.sfs.services;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.valueobjects.EmailAddress;
import com.codexateam.platform.iam.infrastructure.authorization.sfs.model.UserDetailsImpl;
import com.codexateam.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of Spring Security's UserDetailsService.
 * Loads user details from the UserRepository.
 */
@Service("userDetailsServiceImpl")
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a user by their email address (used as username).
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var emailAddress = new EmailAddress(email);
        User user = userRepository.findByEmail(emailAddress) // <--- USE VO
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with email: " + email));
        return UserDetailsImpl.build(user);
    }
}
