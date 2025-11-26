package com.codexateam.platform.listings.application.internal.outboundservices.acl;

import com.codexateam.platform.iam.domain.model.aggregates.User;
import com.codexateam.platform.iam.domain.model.queries.GetUserByIdQuery;
import com.codexateam.platform.iam.domain.model.valueobjects.Roles;
import com.codexateam.platform.iam.domain.services.UserQueryService;
import org.springframework.stereotype.Service;

/**
 * Implementation of ExternalIamService ACL for Listings context.
 * Communicates with the IAM bounded context to validate user permissions.
 *
 * Note: This wraps UserQueryService directly. When IamContextFacade is available,
 * refactor to use it instead for better encapsulation.
 */
@Service
public class ExternalIamServiceImpl implements ExternalIamService {

    private final UserQueryService userQueryService;

    public ExternalIamServiceImpl(UserQueryService userQueryService) {
        this.userQueryService = userQueryService;
    }

    /**
     * Checks if a user exists and has the ROLE_ARRENDADOR role.
     *
     * @param userId The ID of the user to validate.
     * @return true if the user exists and has ROLE_ARRENDADOR, false otherwise.
     */
    @Override
    public boolean isOwner(Long userId) {
        try {
            var userOpt = userQueryService.handle(new GetUserByIdQuery(userId));

            if (userOpt.isEmpty()) {
                return false;
            }

            User user = userOpt.get();
            return user.getRoles().stream()
                    .anyMatch(role -> role.getName() == Roles.ROLE_ARRENDADOR);

        } catch (Exception e) {
            return false;
        }
    }
}

