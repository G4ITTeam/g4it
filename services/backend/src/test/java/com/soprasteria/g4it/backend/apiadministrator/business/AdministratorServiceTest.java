/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;


import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.business.RoleService;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.apiuser.model.*;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static com.soprasteria.g4it.backend.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministratorServiceTest {

    private long organizationId;
    private long subscriberId;

    // Given global
    @InjectMocks
    private AdministratorService administratorService;

    @Mock
    private RoleService roleService;

    @Mock
    private UserService userService;
    @Mock
    private OrganizationService organizationService;
    @Mock
    private OrganizationRepository organizationRepository;
    @Mock
    private UserOrganizationRepository userOrganizationRepository;
    @Mock
    private UserRoleOrganizationRepository userRoleOrganizationRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private SubscriberRepository subscriberRepository;

    private final Organization organization = TestUtils.createOrganization();

    @BeforeEach
    void init() {
        subscriberId = organization.getSubscriber().getId();
        organizationId = organization.getId();
    }

    @Test
    void getSubscribers_activeOrganization() {

        List<Role> orgRole = List.of(Role.builder().name(Constants.ROLE_INVENTORY_READ).build());
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        List<String> subscriberAdminList = List.of(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR);
        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);

        SubscriberBO expectSub = SubscriberBO.builder().id(subscriberId).roles(subscriberAdminList).organizations(
                List.of(OrganizationBO.builder().id(organizationId).build())).build();
        List<SubscriberBO> expectSubList = List.of(expectSub);
        // Only Active Organization should be returned
        List<UserOrganization> userOrgList = List.of(TestUtils.createUserOrganization(organizationId, orgRole, OrganizationStatus.ACTIVE.name()));
        Map<Subscriber, List<UserOrganization>> organizationBySubscriberMap = Map.of(TestUtils.createSubscriber(subscriberId), userOrgList);
        HashMap<Long, UserSubscriber> userSubscriberMap = new HashMap<>();
        userSubscriberMap.put(subscriberId, TestUtils.createUserSubscriber(subscriberId, List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build()), List.of()));

        when(roleService.hasAdminRightsOnAnySubscriber(user)).thenReturn(true);
        when(roleService.getSubscribersWithAdminRole(user)).thenReturn(List.of(subscriberId));
        when(userService.getOrganizationBySubscriberMap(user)).thenReturn(organizationBySubscriberMap);
        when(userService.getUserSubscriberMap(user)).thenReturn(userSubscriberMap);
        when(userService.buildSubscriber(userSubscriberMap.get(subscriberId), userOrgList)).thenReturn(expectSub);

        List<SubscriberBO> subs = administratorService.getSubscribers(user);

        verify(userService, times(1)).buildSubscriber(userSubscriberMap.get(subscriberId), userOrgList);
        assertThat(subs).hasSameSizeAs(expectSubList);
    }


    @Test
    void getSubscribers_noActiveOrganization() {

        List<Role> orgRole = List.of(Role.builder().name(Constants.ROLE_INVENTORY_READ).build());
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        List<SubscriberBO> expectSubList = List.of();
        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        // Create Organization with status 'InActive'.
        List<UserOrganization> userOrgList = List.of(TestUtils.createUserOrganization(organizationId, orgRole, OrganizationStatus.INACTIVE.name()));
        Map<Subscriber, List<UserOrganization>> organizationBySubscriberMap = Map.of(TestUtils.createSubscriber(subscriberId), userOrgList);
        HashMap<Long, UserSubscriber> userSubscriberMap = new HashMap<>();
        userSubscriberMap.put(subscriberId, TestUtils.createUserSubscriber(subscriberId, List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build()), List.of()));

        when(roleService.hasAdminRightsOnAnySubscriber(user)).thenReturn(true);
        when(roleService.getSubscribersWithAdminRole(user)).thenReturn(List.of(subscriberId));
        when(userService.getOrganizationBySubscriberMap(user)).thenReturn(organizationBySubscriberMap);
        when(userService.getUserSubscriberMap(user)).thenReturn(userSubscriberMap);
        //   when(userService.buildSubscriber(userSubscriberMap.get(subscriberId), userOrgList)).thenReturn(expectSub);

        List<SubscriberBO> subs = administratorService.getSubscribers(user);
        assertThat(subs).hasSameSizeAs(expectSubList);
    }

    @Test
    void updateOrganization_deleteActiveOrganization() {

        long dataRetentionDay = 7L;
        LocalDateTime now = LocalDateTime.now();
        String organizationName = "ORGANIZATION";
        String currentStatus = OrganizationStatus.ACTIVE.name();
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        OrganizationBO expectedResult = OrganizationBO.builder().id(organizationId).status(updatedStatus)
                .deletionDate(now.plusDays(dataRetentionDay)).subscriber_id(subscriberId)
                .build();

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        OrganizationUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(subscriberId, organizationName
                , currentStatus, dataRetentionDay);
        when(roleService.hasAdminRightsOnSubscriber(user, subscriberId)).thenReturn(true);
        when(organizationService.updateOrganization(organizationId, organizationUpsertRest, user)).thenReturn(expectedResult);

        administratorService.updateOrganization(organizationId, organizationUpsertRest, user);

        verify(organizationService, times(1)).updateOrganization(organizationId, organizationUpsertRest, user);
        assertEquals(expectedResult.getStatus(), updatedStatus);
        assertEquals(expectedResult.getDeletionDate(), now.plusDays(dataRetentionDay));

    }


    @Test
    void updateOrganization_unDeleteToBeDeletedOrganization() {

        Long dataRetentionDay = 0L;
        String organizationName = "ORGANIZATION";
        String currentStatus = OrganizationStatus.TO_BE_DELETED.name();
        String updatedStatus = OrganizationStatus.ACTIVE.name();
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        OrganizationBO expectedResult = OrganizationBO.builder().name(organizationName).id(organizationId).status(updatedStatus)
                .deletionDate(null).subscriber_id(subscriberId)
                .build();

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        OrganizationUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(subscriberId, organizationName
                , currentStatus, dataRetentionDay);
        when(roleService.hasAdminRightsOnSubscriber(user, subscriberId)).thenReturn(true);
        when(organizationService.updateOrganization(organizationId, organizationUpsertRest, user)).thenReturn(expectedResult);

        administratorService.updateOrganization(organizationId, organizationUpsertRest, user);

        verify(organizationService, times(1)).updateOrganization(organizationId, organizationUpsertRest, user);
        assertEquals(expectedResult.getStatus(), updatedStatus);
        assertNull(expectedResult.getDeletionDate());

    }


    @Test
    void getAllRoles() {

        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);

        when(roleService.getOrganizationsWithAdminRole(user)).thenReturn(List.of());
        when(roleService.getSubscribersWithAdminRole(user)).thenReturn(List.of(1L));
        when(roleService.getAllRoles()).thenReturn(Collections.singletonList(RoleBO.builder().id(1L).name("ROLE_DIGITAL_SERVICE_READ").build()));

        List<RoleBO> roles = administratorService.getAllRoles(user);
        assertEquals(1, roles.size());
    }

    @Test
    void getUsersOfOrg() {


        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);

        when(roleService.hasAdminRightsOnSubscriberOrOrganization(user, subscriberId, organizationId)).thenReturn(true);
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(userOrganizationRepository.findByOrganization(organization)).thenReturn(Collections.singletonList(UserOrganization.builder().user(user).organization(organization).roles(List.of()).build()));

        List<UserInfoBO> users = administratorService.getUsersOfOrg(organizationId, user);

        assertEquals(1, users.size());

    }

    @Test
    void linkUserToOrg_WithRoles() {

        long userId = 1L;

        List<String> roles = Collections.singletonList(ROLE);
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, roles);

        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());
        User currentUser = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);

        when(roleService.hasAdminRightsOnSubscriberOrOrganization(currentUser, subscriberId, organizationId)).thenReturn(true);

        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId, Collections.singletonList(userRoleRest));

        when(userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userId)).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(userRepository.findById(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(roleRepository.findAll()).thenReturn(List.of(Role.builder().id(2L).name(ROLE).build()));

        List<UserInfoBO> users = administratorService.linkUserToOrg(linkUserRoleRest, currentUser);
        assertEquals(1, users.size());
        verify(userOrganizationRepository, times(1)).save(any(UserOrganization.class));
        verify(userRoleOrganizationRepository, times(1)).saveAll(anyList());
    }

    @Test
    void linkUserToOrg_WithoutRoles() {

        long userId = 1L;
        Organization organization = TestUtils.createOrganization();
        UserRoleRest userRoleRest = TestUtils.createUserRoleRest(userId, List.of());
        LinkUserRoleRest linkUserRoleRest = TestUtils.createLinkUserRoleRest(organizationId, Collections.singletonList(userRoleRest));

        User currentUser = TestUtils.createUserWithRoleOnSub(
                subscriberId,
                List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build())
        );

        when(roleService.hasAdminRightsOnSubscriberOrOrganization(currentUser, subscriberId, organizationId)).thenReturn(true);

        when(userOrganizationRepository.findByOrganizationIdAndUserId(organizationId, userRoleRest.getUserId())).thenReturn(Optional.empty());
        when(organizationRepository.findById(organizationId)).thenReturn(Optional.of(organization));
        when(userRepository.findById(userRoleRest.getUserId())).thenReturn(Optional.of(User.builder().id(userId).build()));

        List<UserInfoBO> users = administratorService.linkUserToOrg(linkUserRoleRest, currentUser);
        assertEquals(1, users.size());
        verify(userOrganizationRepository, times(1)).save(any(UserOrganization.class));
    }

    @Test
    void searchUserByName_withNoLinkedOrg() {
        String searchedUser = "stName";

        Organization organization = TestUtils.createOrganization();

        User currentUser = TestUtils.createUserWithRoleOnSub(
                subscriberId,
                List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build())
        );

        when(roleService.hasAdminRightsOnSubscriberOrOrganization(currentUser, subscriberId, organizationId)).thenReturn(true);

        String authorizedDomains = "soprasteria.com,test.com";
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setAuthorizedDomains(authorizedDomains);
        when(subscriberRepository.findById(subscriberId)).thenReturn(Optional.of(subscriber));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(List.of(User.builder().email("testName@soprasteria.com").firstName("test").lastName("Name").build()));
        List<UserSearchBO> searchedUsers;
        searchedUsers = administratorService.searchUserByName(searchedUser, subscriberId, organizationId, currentUser);
        assertEquals(1, searchedUsers.size());

    }

    @Test
    void searchUserByName_withLinkedOrg() {
        String searchedUser = "test";
        User currentUser = TestUtils.createUserWithAdminRoleOnOrg();
        String authorizedDomains = "soprasteria.com,test.com";
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setAuthorizedDomains(authorizedDomains);
        when(roleService.hasAdminRightsOnSubscriberOrOrganization(currentUser, subscriberId, organizationId)).thenReturn(true);
        when(subscriberRepository.findById(subscriberId)).thenReturn(Optional.of(subscriber));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(Collections.singletonList(User.builder().email("test@soprasteria.com")
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name(ROLE).build())).organization(Organization.builder().id(organizationId).name(ORGANIZATION).status(OrganizationStatus.ACTIVE.name())
                                .subscriber(Subscriber.builder().id(2L).name(SUBSCRIBER).build()).build()).build())).build()));
        List<UserSearchBO> searchedUsers;
        searchedUsers = administratorService.searchUserByName(searchedUser, subscriberId, organizationId, currentUser);
        assertEquals(1, searchedUsers.size());
    }
}