/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiadministrator.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.model.UserSearchBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.apiuser.repository.SubscriberRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRepository;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdministratorServiceTest {

    private long organizationId;
    private long subscriberId;

    // Given global
    @InjectMocks
    private AdministratorService administratorService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private AdministratorRoleService administratorRoleService;

    @Mock
    private SubscriberRepository subscriberRepository;

    private final Organization organization = TestUtils.createOrganization();

    @BeforeEach
    void init() {
        subscriberId = organization.getSubscriber().getId();
        organizationId = organization.getId();
        doNothing().when(administratorRoleService).hasAdminRightOnSubscriberOrOrganization(any(), any(), any());
    }


    @Test
    void searchUserByName_withNoLinkedOrg() {
        String searchedUser = "stName";
        Organization organization = TestUtils.createOrganization();

        String authorizedDomains = "soprasteria.com,test.com";
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setAuthorizedDomains(authorizedDomains);

        when(subscriberRepository.findById(any())).thenReturn(Optional.of(subscriber));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(
                List.of(User.builder().email("testName@soprasteria.com").firstName("test").lastName("Name").build()));

        List<UserSearchBO> searchedUsers = administratorService.searchUserByName(searchedUser, subscriberId, organization.getId(),
                TestUtils.createUserBOAdminSub());

        assertEquals(1, searchedUsers.size());
    }

    @Test
    void searchUserByName_withLinkedOrg() {
        String searchedUser = "test";
        String authorizedDomains = "soprasteria.com,test.com";
        Subscriber subscriber = TestUtils.createSubscriber(subscriberId);
        subscriber.setAuthorizedDomains(authorizedDomains);

        when(subscriberRepository.findById(any())).thenReturn(Optional.of(subscriber));
        when(userRepository.findBySearchedName(eq(searchedUser), any())).thenReturn(Collections.singletonList(User
                .builder().email("test@soprasteria.com")
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name(ROLE).build()))
                        .organization(Organization.builder().id(organizationId).name(ORGANIZATION)
                                .status(OrganizationStatus.ACTIVE.name())
                                .subscriber(Subscriber.builder().id(2L).name(SUBSCRIBER).build()).build())
                        .build()))
                .userSubscribers(List.of())
                .build()));
        List<UserSearchBO> searchedUsers;

        searchedUsers = administratorService.searchUserByName(searchedUser, subscriberId, organizationId, TestUtils.createUserBOAdminSub());

        assertEquals(1, searchedUsers.size());
    }


}
