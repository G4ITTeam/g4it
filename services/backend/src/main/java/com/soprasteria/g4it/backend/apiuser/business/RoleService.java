/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.mapper.RoleMapper;
import com.soprasteria.g4it.backend.apiuser.model.RoleBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.RoleRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RoleService {

    /**
     * Repository to access role data.
     */
    @Autowired
    private RoleRepository roleRepository;

    /**
     * Mapper for role data.
     */
    @Autowired
    private RoleMapper roleMapper;

    /**
     * Get list of subscriber's id on which user have 'SUBSCRIBER_ADMINISTRATOR' role.
     *
     * @param user the user.
     * @return list of subscribers.
     */
    public List<Long> getSubscribersWithAdminRole(User user) {
        return user.getUserSubscribers().stream()
                .filter(userSubscriber -> userSubscriber.getUserRoleSubscriber().stream()
                        .anyMatch(userRoleSubscriber -> Constants.ROLE_SUBSCRIBER_ADMINISTRATOR.equals(userRoleSubscriber.getRoles().getName())))
                .map(userSub -> userSub.getSubscriber().getId())
                .toList();
    }

    /**
     * Validate if user have 'SUBSCRIBER_ADMINISTRATOR' role on any subscriber.
     *
     * @param user the user.
     * @return boolean
     */
    public boolean hasAdminRightsOnAnySubscriber(final User user) {
        if (getSubscribersWithAdminRole(user).isEmpty()) {
            log.error("User with id {} do not have admin role on any subscriber", user.getId());
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("User with id '%d' do not have admin role on any subscriber", user.getId()));
        } else
            return true;
    }

    /**
     * Validate if user have 'SUBSCRIBER_ADMINISTRATOR' role on subscriber.
     *
     * @param user         the user.
     * @param subscriberId the subscriber's id.
     * @return boolean
     */
    public boolean hasAdminRightsOnSubscriber(final User user, final Long subscriberId) {
        if (!getSubscribersWithAdminRole(user).contains(subscriberId))
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("User with id '%d' do not have admin role on subscriber with Id : '%d'", user.getId(), subscriberId));
        else
            return true;
    }

    /**
     * Check if user have 'SUBSCRIBER_ADMINISTRATOR' role on any subscriber OR 'ORGANIZATION_ADMINISTRATOR' role
     * on any organization.
     *
     * @param user the user.
     * @return boolean
     */
    public boolean hasAdminRightsOnAnySubscriberOrAnyOrganization(final User user) {
        if (getSubscribersWithAdminRole(user).isEmpty() && getOrganizationsWithAdminRole(user).isEmpty())
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("User with id '%d' do not have admin role on any organization", user.getId()));
        else
            return true;
    }


    /**
     * Check if 'ORGANIZATION_ADMINISTRATOR' exist in user organization table.
     *
     * @param userOrganization user's organization.
     * @return boolean
     */
    public boolean checkIfAdminRoleExistOnOrganization(UserOrganization userOrganization) {
        return userOrganization.getUserRoleOrganization()
                .stream().anyMatch(e -> Constants.ROLE_ORGANIZATION_ADMINISTRATOR.equals(e.getRoles().getName()));
    }

    /**
     * Get list of organization's id on which user have 'ORGANIZATION_ADMINISTRATOR' role.
     *
     * @param user the user.
     * @return list of organizations.
     */
    public List<Long> getOrganizationsWithAdminRole(User user) {
        return user.getUserOrganizations().stream()
                .filter(this::checkIfAdminRoleExistOnOrganization)
                .map(userOrg -> userOrg.getOrganization().getId())
                .toList();
    }

    /**
     * Check if user has 'ORGANIZATION_ADMINISTRATOR' on organization.
     *
     * @param user           the user.
     * @param organizationId the organization's id
     * @return boolean
     */
    public boolean hasAdminRightsOnOrganization(User user, Long organizationId) {
        if (!getOrganizationsWithAdminRole(user).contains(organizationId))
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("User with id '%d' do not have admin role on organization with Id '%d'", user.getId(), organizationId));
        else
            return true;
    }

    public boolean hasAdminRightsOnSubscriberOrOrganization(User user, Long subscriberId, Long organizationId) {
        if (getSubscribersWithAdminRole(user).contains(subscriberId) || getOrganizationsWithAdminRole(user).contains(organizationId))
            return true;
        else
            throw new AuthorizationException(HttpServletResponse.SC_UNAUTHORIZED, String.format("User with id '%d' do not have admin role either on subscriber '%d' or on organization '%d'", user.getId(), subscriberId, organizationId));

    }

    public List<RoleBO> getAllRoles() {
        return roleMapper.toDto(roleRepository.findAll());

    }

}
