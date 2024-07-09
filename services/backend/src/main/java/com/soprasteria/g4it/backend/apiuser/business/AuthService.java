/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.util.Pair;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Auth Service
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuthService {

    private static final Set<String> NOT_DIGITAL_SERVICE = Set.of("device-type", "country", "network-type", "server-host");
    private UserService userService;
    private DigitalServiceRepository digitalServiceRepository;

    /**
     * Gets user's information.
     *
     * @return the user rest object.
     */
    public UserBO getAdminUser() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "The token is not a JWT token");
        }

        UserBO user = userService.getUserFromToken(jwt);
        user.setAdminMode(true);

        UserBO userBO = userService.getUserByName(user);
        if (userBO == null) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "To access to G4IT, you must be added as a member of a organization, please contact your administrator" +
                    "or the support at support.g4it@soprasteria.com.");
        }
        return userBO;
    }

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

        UserBO userBO = userService.getUserByName(userService.getUserFromToken(jwt));
        if (userBO == null) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "To access to G4IT, you must be added as a member of a organization, please contact your administrator" +
                    "or the support at support.g4it@soprasteria.com.");
        }
        return userBO;
    }

    /**
     * Get subscriber and organization
     *
     * @return the pair (sub, org)
     */
    public Pair<String, String> getSubscriberAndOrganization(String[] urlSplit) {
        if (urlSplit == null)
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "Unable to determine associated organization");

        if (urlSplit.length < 5) {
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "Unable to determine associated organization");
        }

        return Pair.of(urlSplit[2], urlSplit[4]);
    }


    /**
     * Verifications of user authentication
     *
     * @return the userBo
     */
    public UserBO verifyUserAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "User is not connected");
        }

        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "The token is not a JWT token");
        }

        return userService.getUserFromToken(jwt);
    }

    /**
     * Get the JwtAuthenticationToken for a user
     *
     * @param userInfo     the userInfo (email, firstName, lastname and subject)
     * @param subscriber   the subscriber
     * @param organization the organization
     * @return the jwt
     */
    @Cacheable(value = "getJwtToken", key = "#userInfo.email + #subscriber + #organization")
    public JwtAuthenticationToken getJwtToken(final UserBO userInfo, final String subscriber, final Long organization) {
        final UserBO user = userService.getUserByName(userInfo);
        if (user == null) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "To access to G4IT, you must be added as a member of a organization, please contact your administrator" +
                    "or the support at support.g4it@soprasteria.com.");
        }

        // Verify authentication and get user roles.
        final List<GrantedAuthority> userRoles = new ArrayList<>(controlAccess(user, subscriber, organization).stream()
                .map(SimpleGrantedAuthority::new)
                .toList());

        // Build new authentication with user roles in database.
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        final JwtAuthenticationToken newAuth = new JwtAuthenticationToken((Jwt) auth.getPrincipal(), userRoles, String.valueOf(user.getId()));
        newAuth.setAuthenticated(true);
        newAuth.setDetails(auth.getDetails());
        return newAuth;
    }


    /**
     * Control user access based on the requestUri.
     *
     * @param user           the user
     * @param subscriberName the subscriberName.
     * @param organizationId the organizationId
     * @return the user roles on ths subscriber and organization containing in the requestURI.
     * @throws AuthorizationException when the user has no role on the subscriber and/or organization.
     */
    private List<String> controlAccess(final UserBO user, final String subscriberName, final Long organizationId) throws AuthorizationException {

        var subscriber = user.getSubscribers().stream()
                .filter(e -> e.getName().equals(subscriberName))
                .findAny()
                .orElseThrow(() -> new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("The subscriber %s is not allowed.", subscriberName)));

        if (subscriber.getRoles().contains(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR)) {
            log.info("UserId={} is authorized for {}/{} with roles={}", user.getId(), subscriber.getName(), organizationId, Constants.ROLE_SUBSCRIBER_ADMINISTRATOR);
            return Constants.ALL_ROLES;
        }

        // Retrieve subscriber from uri, in second position.
        var organization = subscriber.getOrganizations().stream()
                .filter(e -> Objects.equals(e.getId(), organizationId))
                .findAny()
                .orElseThrow(() -> new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("The organization name %s is not allowed.", organizationId)));

        // Active role.
        if (CollectionUtils.isEmpty(organization.getRoles())) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "The user has any role.");
        }

        final List<String> roles = new ArrayList<>(organization.getRoles());
        log.info("UserId={} is authorized for {}/{} with roles={}", user.getId(), subscriber.getName(), organization.getId(), roles);

        if (roles.contains(Constants.ROLE_DIGITAL_SERVICE_WRITE)) {
            roles.add(Constants.ROLE_DIGITAL_SERVICE_READ);
        }
        if (roles.contains(Constants.ROLE_INVENTORY_WRITE)) {
            roles.add(Constants.ROLE_INVENTORY_READ);
        }
        if (roles.contains(Constants.ROLE_ORGANIZATION_ADMINISTRATOR)) {
            roles.addAll(Constants.ALL_BASIC_ROLES);
        }

        return roles;
    }

    /**
     * Check if the user has the right to manage the digitalService
     * Specific cases when the API is for getting device-type, network-host, ... (see the constant NOT_DIGITAL_SERVICE)
     * - correlated with APIs in digital-service-referential.yml
     *
     * @param urlSplit the split url
     */
    public void checkUserRightForDigitalService(String[] urlSplit) {
        // urlSplit looks like this :
        // [ "", "subscribers", "$subscriber", "organizations", "$organization", "digital-services", "$digitalServiceUid", ...restOfUri ]
        if (urlSplit.length <= 6) return;
        if (!"digital-services".equals(urlSplit[5])) return;

        final String digitalServiceUid = urlSplit[6];
        if (NOT_DIGITAL_SERVICE.contains(digitalServiceUid)) return;

        if (!digitalServiceRepository.existsByUidAndUserId(digitalServiceUid, getUser().getId())) {
            throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "The user has no right to manage this digitalService");
        }
    }
}
