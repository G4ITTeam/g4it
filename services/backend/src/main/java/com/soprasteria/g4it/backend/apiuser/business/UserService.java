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
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * User Service
 */
@Service
@Slf4j
public class UserService {

    @Autowired
    OrganizationService organizationService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SubscriberRepository subscriberRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private NewUserService newUserService;
    @Autowired
    private CacheManager cacheManager;
    /**
     * The repository to access organization data.
     */
    @Autowired
    private UserSubscriberRepository userSubscriberRepository;

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

        return getUserByName(getUserFromToken(jwt));
    }

    /**
     * Create a UserBo object from jwt token
     *
     * @param jwt the token
     * @return the userBo
     */
    public UserBO getUserFromToken(final Jwt jwt) {
        final String email = jwt.getClaim(Constants.JWT_EMAIL_FIELD);
        return UserBO.builder()
                .email(email)
                .firstName(jwt.getClaim(Constants.JWT_FIRST_NAME))
                .lastName(jwt.getClaim(Constants.JWT_LAST_NAME))
                .sub(jwt.getClaim(Constants.JWT_SUB))
                .domain(email.split("@")[1])
                .build();
    }

    /**
     * Gets user's information.
     *
     * @return the user rest object.
     */
    public User getUserEntity() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "The token is not a JWT token");
        }

        return userRepository.findByEmail(jwt.getClaim(Constants.JWT_EMAIL_FIELD)).orElseThrow();
    }

    /**
     * Gets user's information by its name.
     *
     * @param userInfo the userInfo (email, firstName, lastname and subject)
     * @return the user rest object.
     */
    @Transactional
    public UserBO getUserByName(final UserBO userInfo) {
        String sub = userInfo.getSub();
        String email = userInfo.getEmail();
        Optional<User> userSubject = userRepository.findBySub(sub);
        User userReturned = null;
        if (userSubject.isPresent()) {
            // sub present with diff email address: user info in g4it_user with new values
            userReturned = userSubject.get();
            if (!email.equals(userReturned.getEmail()) || !userInfo.getLastName().equals(userReturned.getLastName())) {
                updateUserWithNewUserinfo(userReturned, userInfo);
            }
            if (userReturned.getUserOrganizations().stream().filter(org -> org.getOrganization().getStatus().equals(OrganizationStatus.ACTIVE.name())).toList().isEmpty()) {
                throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, "To access to G4IT, you must be added as a member of a organization, please contact your administrator" +
                        "or the support at support.g4it@soprasteria.com.");
            }
        }

        if (userReturned == null) {
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                // sub not present but has email address: user info in g4it_user with new values
                userReturned = updateUserWithNewUserinfo(userOptional.get(), userInfo);
            } else {
                List<Role> accessRoles = List.of(
                        roleRepository.findByName(Constants.ROLE_INVENTORY_READ),
                        roleRepository.findByName(Constants.ROLE_DIGITAL_SERVICE_READ),
                        roleRepository.findByName(Constants.ROLE_DIGITAL_SERVICE_WRITE)
                );

                User newUser = createNewUserWithDomain(accessRoles, userInfo);

                if (newUser == null) {
                    // organization doesn't exist in g4it_subscriber, add new user to g4it_user with no rights
                    newUserService.createNewUser(userInfo);
                    Objects.requireNonNull(cacheManager.getCache("findBySub")).evict(sub);

                    throw new AuthorizationException(HttpServletResponse.SC_FORBIDDEN, "To access to G4IT, you must be added as a member of a organization, please contact your administrator" +
                            "or the support at support.g4it@soprasteria.com.");
                } else
                    userReturned = userRepository.findById(newUser.getId()).orElseThrow();
            }

        }

        return UserBO.builder()
                .id(userReturned.getId())
                .firstName(userReturned.getFirstName())
                .lastName(userReturned.getLastName())
                .email(userReturned.getEmail())
                .subscribers(buildSubscribers(userReturned))
                .build();
    }

    /**
     * Create new user with authorized domains
     *
     * @param accessRoles the default access roles
     * @param userInfo    the user info
     * @return the new user created
     */
    private User createNewUserWithDomain(List<Role> accessRoles, UserBO userInfo) {
        User newUser = null;
        for (Subscriber subscriber : subscriberRepository.findByAuthorizedDomainsNotNull()) {
            if (Arrays.asList(subscriber.getAuthorizedDomains().split(",")).contains(userInfo.getDomain())) {
                Organization demoOrg = organizationRepository.findBySubscriberNameAndName(subscriber.getName(), Constants.DEMO)
                        .orElseGet(() -> organizationRepository.save(Organization.builder()
                                .name(Constants.DEMO)
                                .creationDate(LocalDateTime.now())
                                .subscriber(subscriber)
                                .build()));
                newUser = newUserService.createUser(subscriber, demoOrg, newUser, userInfo, accessRoles);
                Objects.requireNonNull(cacheManager.getCache("findBySub")).evict(userInfo.getSub());
            }
        }
        return newUser;
    }

    /**
     * update user.
     *
     * @param user     the user.
     * @param userInfo the user's information
     * @return the user with updated user info.
     */
    private User updateUserWithNewUserinfo(final User user, final UserBO userInfo) {
        user.setSub(userInfo.getSub());
        user.setEmail(userInfo.getEmail());
        user.setFirstName(userInfo.getFirstName());
        user.setLastName(userInfo.getLastName());
        user.setDomain(userInfo.getDomain());
        userRepository.save(user);
        return user;
    }

    /**
     * Build subscriber list.
     *
     * @param user the user.
     * @return the user's subscriber list.
     */
    private List<SubscriberBO> buildSubscribers(final User user) {

        if (user.getUserSubscribers() == null || user.getUserOrganizations() == null) return List.of();

        // (subscriber, [userOrganization])
        final Map<Subscriber, List<UserOrganization>> organizationBySubscriber = getOrganizationBySubscriberMap(user);

        // subscriber: (id, userSubscriber)
        final var userSubscriberMap = getUserSubscriberMap(user);

        return organizationBySubscriber.entrySet().stream()
                .map(orgBySub -> {
                    List<UserOrganization> userOrgList = orgBySub.getValue().stream()
                            .filter(userOrg -> userOrg.getOrganization().getStatus().equals(OrganizationStatus.ACTIVE.name()))
                            .toList();
                    return userSubscriberMap.containsKey(orgBySub.getKey().getId()) ?
                            buildSubscriber(userSubscriberMap.get(orgBySub.getKey().getId()), userOrgList) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Get the hashmap containing the subscriber as key and list of user organizations object as value.
     *
     * @param user the user.
     * @return Map<Subscriber, List < UserOrganization>>
     */
    public Map<Subscriber, List<UserOrganization>> getOrganizationBySubscriberMap(User user) {
        return user.getUserOrganizations().stream()
                .collect(Collectors.groupingBy(e -> e.getOrganization().getSubscriber()));
    }

    /**
     * Get the hashmap containing the subscriber's id as key and UserSubscriber object as value.
     *
     * @param user the user
     * @return HashMap<Long, UserSubscriber>
     */
    public HashMap<Long, UserSubscriber> getUserSubscriberMap(User user) {
        final var userSubscriberMap = new HashMap<Long, UserSubscriber>();
        for (final var userSubscriber : user.getUserSubscribers()) {
            userSubscriberMap.put(userSubscriber.getSubscriber().getId(), userSubscriber);
        }
        return userSubscriberMap;
    }


    /**
     * Build subscriber.
     *
     * @param userSubscriber    the user subscriber.
     * @param userOrganizations the user's organization list.
     * @return the user's subscriber.
     */
    public SubscriberBO buildSubscriber(final UserSubscriber userSubscriber, final List<UserOrganization> userOrganizations) {
        return SubscriberBO.builder()
                .defaultFlag(userSubscriber.getDefaultFlag())
                .name(userSubscriber.getSubscriber().getName())
                .organizations(userOrganizations.stream()
                        .map(this::buildOrganization)
                        .toList())
                .roles(userSubscriber.getRoles().stream().map(Role::getName).toList())
                .id(userSubscriber.getSubscriber().getId())
                .build();
    }

    /**
     * Build organization.
     *
     * @param userOrganization the user's organization.
     * @return the user's organization.
     */
    public OrganizationBO buildOrganization(final UserOrganization userOrganization) {
        return OrganizationBO.builder()
                .roles(userOrganization.getRoles().stream().map(Role::getName).toList())
                .defaultFlag(userOrganization.getDefaultFlag())
                .name(userOrganization.getOrganization().getName())
                .id(userOrganization.getOrganization().getId())
                .status(userOrganization.getOrganization().getStatus())
                .deletionDate(userOrganization.getOrganization().getDeletionDate())
                .build();
    }

}
