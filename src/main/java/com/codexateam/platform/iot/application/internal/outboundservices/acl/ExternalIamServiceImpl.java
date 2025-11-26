package com.codexateam.platform.iot.application.internal.outboundservices.acl;

import com.codexateam.platform.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Implementation of the ExternalIamService ACL for IoT bounded context.
 * Provides access to user information from the IAM context.
 */
@Service("iotExternalIamServiceImpl")
public class ExternalIamServiceImpl implements ExternalIamService {

    private final UserRepository userRepository;

    public ExternalIamServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<String> getUserFullName(Long userId) {
        return userRepository.findById(userId)
                .map(com.codexateam.platform.iam.domain.model.aggregates.User::getName);
    }
}

