/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.SubscriberService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.model.*;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Organization service.
 */
@Service
@AllArgsConstructor
public class AdministratorService {

    /**
     * The Role Service
     */
    @Autowired
    RoleService roleService;
    /**
     * The Subscriber Service
     */
    @Autowired
    SubscriberService subscriberService;
    /**
     * The Organization Service
     */
    @Autowired
    private OrganizationService organizationService;
    /**
     * The User Service
     */
    @Autowired
    private UserService userService;

    /**
     * Repository to access organization data.
     */
    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * Repository to access userOrganization data.
     */
    @Autowired
    private UserOrganizationRepository userOrganizationRepository;

    /**
     * Repository to access userRoleOrganization data.
     */
    @Autowired
    private UserRoleOrganizationRepository userRoleOrganizationRepository;

    /**
     * Repository to access subscriber data.
     */
    @Autowired
    private SubscriberRepository subscriberRepository;

    /**
     * Repository to access user data.
     */
    @Autowired
    private UserRepository userRepository;

    /**
     * Repository to access role data.
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Retrieve the list of subscribers for the user.
     *
     * @param user the user.
     * @return the list of allowed subscribers
     */
    public List<SubscriberBO> getSubscribers(final User user) {
        roleService.hasAdminRightsOnAnySubscriber(user);

        final var userSubscriberMap = userService.getUserSubscriberMap(user);
        List<Long> organizationWithAdminRole = roleService.getSubscribersWithAdminRole(user);

        // Get all subscribers with role "SUBSCRIBER_ADMINISTRATOR"
        return userService.getOrganizationBySubscriberMap(user).entrySet().stream()
                .filter(orgBySubRole -> organizationWithAdminRole.contains(orgBySubRole.getKey().getId()))
                .map(orgBySub -> {
                    List<UserOrganization> orgList = orgBySub.getValue().stream()
                            .filter(orgStatus -> !orgStatus.getOrganization().getStatus().equals(OrganizationStatus.INACTIVE.name()))
                            .toList();
                    if (!orgList.isEmpty())
                        return userService.buildSubscriber(userSubscriberMap.get(orgBySub.getKey().getId()), orgList);
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }


    /**
     * Get all organizations with Admin Role attached to all subscribers.
     *
     * @param user tThe user.
     * @return the SubscriberBO list.
     */

    public List<SubscriberBO> getSubscribersAndOrganizationWithAdminRole(final User user) {
        // check if user can access this API
        roleService.hasAdminRightsOnAnySubscriberOrAnyOrganization(user);
        List<Long> subscribersWithAdminRoleId = roleService.getSubscribersWithAdminRole(user);

        // Get all organizations with role "ORGANIZATION_ADMINISTRATOR"
        List<Long> organizationWithAdminRole = roleService.getOrganizationsWithAdminRole(user);
        // Get all subscribers with role "SUBSCRIBER_ADMINISTRATOR"
        final Map<Subscriber, List<UserOrganization>> organizationBySubscriberMap = userService.getOrganizationBySubscriberMap(user);
        final var userSubscriberMap = userService.getUserSubscriberMap(user);

        // get all organizations from subscriber with admin role.
        List<SubscriberBO> subBOList = new ArrayList<>();
        if (!ObjectUtils.isEmpty(subscribersWithAdminRoleId)) {
            subBOList = organizationBySubscriberMap.entrySet().stream()
                    .filter(sub -> subscribersWithAdminRoleId.contains(sub.getKey().getId()))
                    .map(orgBySub -> {
                        List<UserOrganization> userOrgList = orgBySub.getValue().stream()
                                .filter(orgStatus -> orgStatus.getOrganization().getStatus().equals(OrganizationStatus.ACTIVE.name()))
                                .toList();
                        if (!userOrgList.isEmpty())
                            return userService.buildSubscriber(userSubscriberMap.get(orgBySub.getKey().getId()), userOrgList);
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toCollection(ArrayList::new));
        }


        List<SubscriberBO> additionalSubBOList = organizationBySubscriberMap.entrySet().stream()
                .filter(sub -> !subscribersWithAdminRoleId.contains(sub.getKey().getId()))
                .map(orgBySub -> {
                    List<UserOrganization> userOrgList = orgBySub.getValue().stream()
                            .filter(org -> organizationWithAdminRole.contains(org.getOrganization().getId()))
                            .filter(orgStatus -> orgStatus.getOrganization().getStatus().equals(OrganizationStatus.ACTIVE.name()))
                            .toList();
                    if (!userOrgList.isEmpty())
                        return userService.buildSubscriber(userSubscriberMap.get(orgBySub.getKey().getId()), userOrgList);
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
        subBOList.addAll(additionalSubBOList);
        return subBOList;
    }

    /**
     * Get the list of active organizations with admin role attached to subscriber
     * to be displayed in 'manage users' screen.
     * filter using subscriber or organization or both.
     *
     * @param subscriberId   the client subscriber id.
     * @param organizationId the organization id.
     * @param user           the user.
     * @return list of SubscriberBO.
     */
    public List<SubscriberBO> getOrganizations(final Long subscriberId, final Long organizationId, final User user) {

        if (organizationId == null && subscriberId == null) {
            // fetch all subscribers linked to this user
            return this.getSubscribersAndOrganizationWithAdminRole(user);
        }

        // filtering based on subscriber & organization.
        validateSubscriberAndOrganization(subscriberId, organizationId, user);

        final var userSubscriberMap = userService.getUserSubscriberMap(user);

        List<SubscriberBO> subBOList = userService.getOrganizationBySubscriberMap(user).entrySet().stream()
                .filter(sub -> subscriberId == null || Objects.equals(subscriberId, sub.getKey().getId()))
                .map(orgBySub -> getSubscriberBO(organizationId, orgBySub, userSubscriberMap, user))
                .filter(Objects::nonNull)
                .toList();

        if (subBOList.isEmpty())
            throw new G4itRestException("404", String.format("organization %d is not attached with subscriber %d", organizationId, subscriberId));
        else
            return subBOList;
    }

    private SubscriberBO getSubscriberBO(final Long organizationId, final Map.Entry<Subscriber, List<UserOrganization>> orgBySub, final HashMap<Long, UserSubscriber> userSubscriberMap, final User user) {
        final List<Long> organizationWithAdminRole = roleService.getOrganizationsWithAdminRole(user);
        List<UserOrganization> userOrgList = orgBySub.getValue().stream()
                .filter(org -> organizationId == null || organizationId == org.getOrganization().getId())
                .filter(org -> organizationWithAdminRole.contains(org.getOrganization().getId()))
                .filter(org -> OrganizationStatus.ACTIVE.name().equals(org.getOrganization().getStatus()))
                .toList();

        if (!userOrgList.isEmpty())
            return userService.buildSubscriber(userSubscriberMap.get(orgBySub.getKey().getId()), userOrgList);
        return null;
    }


    /**
     * Validate the Admin Roles and Ids
     *
     * @param subscriberId   the subscriber id.
     * @param organizationId the organization id.
     * @param user           the user.
     */
    private void validateSubscriberAndOrganization(final Long subscriberId, final Long organizationId, final User user) {

        if (subscriberId == null) {
            // Check if organization exists.
            organizationService.getOrganizationByStatus(null, organizationId, List.of(OrganizationStatus.ACTIVE.name()));
            // validate if user has admin role on this organization
            roleService.hasAdminRightsOnOrganization(user, organizationId);
            return;
        }

        // validate valid subscriber id
        subscriberService.getSubscriptionById(subscriberId);
        if (Objects.nonNull(organizationId)) {
            // Only organization with status 'Active' should be displayed on 'manage-users' screen.
            organizationService.getOrganizationByStatus(subscriberId, organizationId, List.of(OrganizationStatus.ACTIVE.name()));
            // validate if user has admin role on this organization
            roleService.hasAdminRightsOnOrganization(user, organizationId);
        } else {
            // validate if user has admin role on subscriber
            roleService.hasAdminRightsOnSubscriber(user, subscriberId);
            // validate if user has admin role on any organization.
            List<Long> organizationWithAdminRole = roleService.getOrganizationsWithAdminRole(user);
            // If user do not have admin role on any organization then either throw exception
            // or empty list.
            if (organizationWithAdminRole.isEmpty())
                throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("User with id '%d' do not have admin role on any organization", user.getId()));

        }
    }


    /**
     * Update the organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param user                   the user.
     * @return OrganizationBO
     */
    public OrganizationBO updateOrganization(final Long organizationId, final OrganizationUpsertRest organizationUpsertRest, User user) {
        roleService.hasAdminRightsOnSubscriber(user, organizationUpsertRest.getSubscriberId());
        return organizationService.updateOrganization(organizationId, organizationUpsertRest, user);
    }

    /**
     * Create an Organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param user                   the user.
     * @return organization BO.
     */
    public OrganizationBO createOrganization(OrganizationUpsertRest organizationUpsertRest, User user) {
        Long subscriberId = organizationUpsertRest.getSubscriberId();

        // Check Admin Role on this subscriber.
        roleService.hasAdminRightsOnSubscriber(user, subscriberId);

        Subscriber subscriber = subscriberService.getSubscriptionById(subscriberId);
        return organizationService.createOrganization(organizationUpsertRest, user, subscriber);
    }

    /**
     * Get list of all the Roles from g4it_role
     *
     * @param user the current user.
     */
    public List<RoleBO> getAllRoles(final User user) {
        List<Long> organizationsWithAdminRole = roleService.getOrganizationsWithAdminRole(user);
        List<Long> subscribersWithAdminRole = roleService.getSubscribersWithAdminRole(user);
        if (organizationsWithAdminRole.isEmpty() && subscribersWithAdminRole.isEmpty()) {
            throw new G4itRestException("404", String.format("User with id '%d' do not have admin role neither on any subscriber nor on any organization.", user.getId()));

        }
        return roleService.getAllRoles();

    }

    /**
     * Get list of all the users linked to an organization
     *
     * @param organizationId the organization id.
     * @param user           the current user
     */
    public List<UserInfoBO> getUsersOfOrg(Long organizationId, final User user) {
        long subscriberId = organizationRepository.findById(organizationId).orElseThrow().getSubscriber().getId();
        roleService.hasAdminRightsOnSubscriberOrOrganization(user, subscriberId, organizationId);
        Optional<Organization> orgOpt = organizationRepository.findById(organizationId);
        if (orgOpt.isEmpty()) {
            throw new G4itRestException("404", String.format("Organization %d not found.", organizationId));
        }

        return userOrganizationRepository.findByOrganization(orgOpt.get()).stream()
                .map(userOrganization -> {
                    UserInfoBO userInfoBO = UserInfoBO.builder()
                            .id(userOrganization.getUser().getId())
                            .firstName(userOrganization.getUser().getFirstName())
                            .lastName(userOrganization.getUser().getLastName())
                            .email(userOrganization.getUser().getEmail())
                            .roles(userOrganization.getRoles().stream().map(Role::getName).toList())
                            .build();
                    return userInfoBO;
                })
                .toList();

    }

    /**
     * link user to an organization
     *
     * @param linkUserRoleRest the linkUserRoleRest
     * @param currentUser      the current user
     */
    public List<UserInfoBO> linkUserToOrg(final LinkUserRoleRest linkUserRoleRest, final User currentUser) {

        Long organizationId = linkUserRoleRest.getOrganizationId();
        final Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("OrganizationId %s is not found in database", organizationId)));

        roleService.hasAdminRightsOnSubscriberOrOrganization(currentUser, organization.getSubscriber().getId(), organizationId);

        List<UserInfoBO> userInfoList = new ArrayList<>();

        List<Role> allRoles = roleRepository.findAll();

        for (UserRoleRest userRoleRest : linkUserRoleRest.getUsers()) {
            User userEntity = userRepository.findById(userRoleRest.getUserId()).orElseThrow();

            if (userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userRoleRest.getUserId()).isPresent()) {
                throw new G4itRestException("409", String.format("user  %s is already linked to the organization.", userEntity.getEmail()));
            }

            UserOrganization userOrgToCreate = UserOrganization.builder().
                    organization(organization)
                    .user(userEntity)
                    .defaultFlag(true)
                    .build();

            userOrganizationRepository.save(userOrgToCreate);

            if (userRoleRest.getRoles() != null) {
                List<UserRoleOrganization> userRoleOrganizations = userRoleRest.getRoles().stream()
                        .map(userRole -> allRoles.stream()
                                .filter(r -> r.getName().equals(userRole))
                                .map(foundRole -> {
                                    UserRoleOrganization userRoleOrganization = UserRoleOrganization.builder()
                                            .userOrganizations(userOrgToCreate)
                                            .roles(foundRole)
                                            .build();
                                    return userRoleOrganization;
                                })
                                .findFirst().orElse(null))
                        .filter(Objects::nonNull)
                        .toList();

                userRoleOrganizationRepository.saveAll(userRoleOrganizations);
            }

            // Create and add UserInfoBO to the list
            userInfoList.add(UserInfoBO.builder()
                    .id(userRoleRest.getUserId())
                    .firstName(userEntity.getFirstName())
                    .lastName(userEntity.getLastName())
                    .email(userEntity.getEmail())
                    .roles(userRoleRest.getRoles())
                    .build());
        }

        return userInfoList;
    }

    /**
     * Get all the users( filtered by authorized_domains of subscriber)
     *
     * @param searchedName the string to be searched
     * @param subscriberId the subscriber's id
     * @param currentUser  the current user
     */
    public List<UserSearchBO> searchUserByName(final String searchedName,
                                               final Long subscriberId,
                                               final Long organizationId,
                                               final User currentUser) {
        roleService.hasAdminRightsOnSubscriberOrOrganization(currentUser, subscriberId, organizationId);
        Optional<Subscriber> subsOpt = subscriberRepository.findById(subscriberId);
        if (subsOpt.isEmpty()) {
            throw new G4itRestException("404", String.format("Subscriber %d not found.", subscriberId));
        }

        if (subsOpt.get().getAuthorizedDomains() == null) return List.of();

        Set<String> domains = Arrays.stream(subsOpt.get().getAuthorizedDomains().replaceAll("\\s+", "").split(","))
                .collect(Collectors.toSet());

        List<User> searchedList = userRepository.findBySearchedName(searchedName, domains);
        if (searchedList.isEmpty()) return List.of();

        return searchedList.stream()
                .map(user -> {
                    UserSearchBO userSearch = UserSearchBO.builder()
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .email(user.getEmail())
                            .linkedOrgIds(getUserOrganizationIds(user))
                            .build();
                    return userSearch;
                })
                .toList();
    }

    /**
     * Get all the linked organizations' Ids if linked with user
     *
     * @param user the current user
     */
    private List<Long> getUserOrganizationIds(final User user) {
        if (user.getUserOrganizations() == null) return List.of();
        return user.getUserOrganizations().stream()
                .map(userOrg -> userOrg.getOrganization().getId())
                .toList();
    }
}