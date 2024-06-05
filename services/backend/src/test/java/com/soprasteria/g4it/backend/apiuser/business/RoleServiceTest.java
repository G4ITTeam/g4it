/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.AuthorizationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoleService roleService;
    public static Long SUBSCRIBER_ID = 1L;
    public static Long ORGANIZATION_ID = 1L;
    public static String OrgActiveStatus = OrganizationStatus.ACTIVE.name();
    public static List<Role> orgRole = List.of(Role.builder().name(Constants.ROLE_INVENTORY_READ).build());
    public static List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
    public static List<String> subscriberAdminList = List.of(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR);

    public static Role subAdminRole = Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build();
    public static Role orgAdminRole = Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build();
    public static Role subNonAdminRole = Role.builder().name("").build();

    @Test
    void hasAdminRightsOnAnySubscriber_hasAdminRole() {
        List<UserRoleSubscriber> userRoleSubList = List.of(TestUtils.createUserRoleSubscriber(subAdminRole));
        List<UserRoleSubscriber> userRoleSubListWithoutAdminRole = List.of(TestUtils.createUserRoleSubscriber(subNonAdminRole));
        List<UserRoleOrganization> userRoleOrgList = List.of(TestUtils.createUserRoleOrganization(orgAdminRole));
        List<UserOrganization> userOrgList = List.of(TestUtils.createUserOrganization(SUBSCRIBER_ID, ORGANIZATION_ID, orgRole, OrgActiveStatus, LocalDateTime.now(), userRoleOrgList));
        List<UserSubscriber> userSubList = List.of(TestUtils.createUserSubscriber(SUBSCRIBER_ID, subscriberAdminRole, userRoleSubList),
                TestUtils.createUserSubscriber(2L, List.of(), userRoleSubListWithoutAdminRole));

        User user = TestUtils.createUser(userSubList, userOrgList);

        var result = roleService.hasAdminRightsOnAnySubscriber(user);
        assertTrue(result);
    }

    @Test
    void hasAdminRightsOnAnySubscriber_NoAdminRole() {
        List<UserRoleSubscriber> userRoleSubList = List.of(TestUtils.createUserRoleSubscriber(subAdminRole));
        List<UserRoleSubscriber> userRoleSubListWithoutAdminRole = List.of(TestUtils.createUserRoleSubscriber(subNonAdminRole));
        List<UserRoleOrganization> userRoleOrgList = List.of(TestUtils.createUserRoleOrganization(orgAdminRole));
        List<UserOrganization> userOrgList = List.of(TestUtils.createUserOrganization(SUBSCRIBER_ID, ORGANIZATION_ID, orgRole, OrgActiveStatus, LocalDateTime.now(), userRoleOrgList));
        List<UserSubscriber> userSubList = List.of(TestUtils.createUserSubscriber(SUBSCRIBER_ID, subscriberAdminRole, userRoleSubListWithoutAdminRole),
                TestUtils.createUserSubscriber(2L, List.of(), userRoleSubListWithoutAdminRole));

        User user = TestUtils.createUser(userSubList, userOrgList);

        assertThatThrownBy(() -> roleService.hasAdminRightsOnAnySubscriber(user))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining("do not have admin role on any subscriber");

    }

    @Test
    void hasAdminRightsOnSubscriber_hasAdminRole() {
        List<UserRoleSubscriber> userRoleSubList = List.of(TestUtils.createUserRoleSubscriber(subAdminRole));
        List<UserRoleSubscriber> userRoleSubListWithoutAdminRole = List.of(TestUtils.createUserRoleSubscriber(subNonAdminRole));
        List<UserRoleOrganization> userRoleOrgList = List.of(TestUtils.createUserRoleOrganization(orgAdminRole));
        List<UserOrganization> userOrgList = List.of(TestUtils.createUserOrganization(SUBSCRIBER_ID, ORGANIZATION_ID, orgRole, OrgActiveStatus, LocalDateTime.now(), userRoleOrgList));
        List<UserSubscriber> userSubList = List.of(TestUtils.createUserSubscriber(SUBSCRIBER_ID, subscriberAdminRole, userRoleSubList),
                TestUtils.createUserSubscriber(2L, List.of(), userRoleSubListWithoutAdminRole));

        User user = TestUtils.createUser(userSubList, userOrgList);

        var result = roleService.hasAdminRightsOnSubscriber(user, SUBSCRIBER_ID);
        assertTrue(result);
    }

    @Test
    void hasAdminRightsOnSubscriber_NoAdminRole() {
        List<UserRoleSubscriber> userRoleSubList = List.of(TestUtils.createUserRoleSubscriber(subAdminRole));
        List<UserRoleSubscriber> userRoleSubListWithoutAdminRole = List.of(TestUtils.createUserRoleSubscriber(subNonAdminRole));
        List<UserRoleOrganization> userRoleOrgList = List.of(TestUtils.createUserRoleOrganization(orgAdminRole));
        List<UserOrganization> userOrgList = List.of(TestUtils.createUserOrganization(SUBSCRIBER_ID, ORGANIZATION_ID, orgRole, OrgActiveStatus, LocalDateTime.now(), userRoleOrgList));
        List<UserSubscriber> userSubList = List.of(TestUtils.createUserSubscriber(SUBSCRIBER_ID, subscriberAdminRole, userRoleSubListWithoutAdminRole),
                TestUtils.createUserSubscriber(2L, List.of(), userRoleSubListWithoutAdminRole));

        User user = TestUtils.createUser(userSubList, userOrgList);

        assertThatThrownBy(() -> roleService.hasAdminRightsOnSubscriber(user, SUBSCRIBER_ID))
                .isInstanceOf(AuthorizationException.class)
                .hasMessageContaining(" do not have admin role on subscriber with Id : '1'");

    }
}
