/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * User Service
 */
@Service
public class UserService {

    /**
     * The user repository to access user data in database.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Gets user's information.
     *
     * @return the user rest object.
     */
    public UserBO getUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "The token is not a JWT token");
        }

        return getUserByName(jwt.getClaim("unique_name"));
    }

    /**
     * Gets user's information by its name.
     *
     * @return the user rest object.
     */
    public UserBO getUserByName(String uniqueName) {
        return userRepository.findByUsername(uniqueName).map(user -> UserBO.builder()
                .username(user.getUsername())
                .subscribers(buildSubscribers(user))
                .build()).orElseThrow();
    }

    /**
     * Gets user's id.
     *
     * @return the user id
     */
    public Long getUserId(String uniqueName) {
        return userRepository.findByUsername(uniqueName).map(User::getId).orElseThrow();
    }

    /**
     * Build subscriber list.
     *
     * @param user the user.
     * @return the user's subscriber list.
     */
    private List<SubscriberBO> buildSubscribers(final User user) {

        // (subscriber, [userOrganization])
        final Map<Subscriber, List<UserOrganization>> organizationBySubscriber = user.getUserOrganizations().stream()
                .collect(Collectors.groupingBy(e -> e.getOrganization().getSubscriber()));

        // subscriber: (id, defaultFlag)
        final var subscriberIdDefaultFlagMap = new HashMap<Long, Boolean>();

        for (final var userSubscriber : user.getUserSubscribers()) {
            subscriberIdDefaultFlagMap.put(userSubscriber.getSubscriber().getId(), userSubscriber.getDefaultFlag());
        }

        return organizationBySubscriber.entrySet().stream()
                .map(orgBySub -> buildSubscriber(orgBySub.getKey().getName(), subscriberIdDefaultFlagMap.getOrDefault(orgBySub.getKey().getId(), false), orgBySub.getValue()))
                .toList();
    }

    /**
     * Build subscriber.
     *
     * @param subscriberName    the subscriber name
     * @param defaultFlag       the default flag
     * @param userOrganizations the user's organization list.
     * @return the user's subscriber.
     */
    private SubscriberBO buildSubscriber(final String subscriberName, final Boolean defaultFlag, final List<UserOrganization> userOrganizations) {
        return SubscriberBO.builder()
                .defaultFlag(defaultFlag)
                .name(subscriberName)
                .organizations(userOrganizations.stream()
                        .map(this::buildOrganization)
                        .toList())
                .build();
    }

    /**
     * Build organization.
     *
     * @param userOrganization the user's organization.
     * @return the user's organization.
     */
    private OrganizationBO buildOrganization(final UserOrganization userOrganization) {
        return OrganizationBO.builder()
                .roles(userOrganization.getRoles().stream().map(Role::getName).toList())
                .defaultFlag(userOrganization.getDefaultFlag())
                .name(userOrganization.getOrganization().getName())
                .build();
    }
}
