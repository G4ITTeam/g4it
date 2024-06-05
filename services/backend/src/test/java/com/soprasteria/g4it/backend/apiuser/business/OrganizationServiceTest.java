/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationMapperImpl;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @InjectMocks
    private OrganizationService organizationService;

    @Mock
    OrganizationRepository organizationRepository;

    public static Long SUBSCRIBER_ID = 1L;
    public static Long ORGANIZATION_ID = 1L;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(organizationService, "organizationMapper", new OrganizationMapperImpl());
    }


    @Test
    void updateOrganization_setStatustoToBeDeleted() {
        Long subscriberId = 1L;
        long organizationId = 1L;
        long dataRetentionDay = 7L;
        LocalDateTime now = LocalDateTime.now();
        String organizationName = "ORGANIZATION";
        String currentStatus = OrganizationStatus.ACTIVE.name();
        String updatedStatus = OrganizationStatus.TO_BE_DELETED.name();
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        Optional<Organization> organizationEntity = Optional.of(Organization.builder().id(organizationId).status(currentStatus)
                .deletionDate(now.plusDays(dataRetentionDay))
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());
        OrganizationUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , updatedStatus, dataRetentionDay);

        when(organizationRepository.findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, Constants.ORGANIZATION_ACTIVE_STATUS)).thenReturn(organizationEntity);

        OrganizationBO orgBO = organizationService.updateOrganization(ORGANIZATION_ID, organizationUpsertRest, user);

        assertEquals(updatedStatus, orgBO.getStatus());
    }

    @Test
    void updateOrganization_setStatustoToActive() {
        Long subscriberId = 1L;
        long organizationId = 1L;
        long dataRetentionDay = 7L;
        String organizationName = "ORGANIZATION";
        String currentStatus = OrganizationStatus.TO_BE_DELETED.name();
        String updatedStatus = OrganizationStatus.ACTIVE.name();
        List<Role> subscriberAdminRole = List.of(Role.builder().name(Constants.ROLE_SUBSCRIBER_ADMINISTRATOR).build());

        User user = TestUtils.createUserWithRoleOnSub(subscriberId, subscriberAdminRole);
        Optional<Organization> organizationEntity = Optional.of(Organization.builder().name(organizationName).id(organizationId).status(currentStatus)
                .deletionDate(null)
                .subscriber(Subscriber.builder().id(SUBSCRIBER_ID).build())
                .build());
        OrganizationUpsertRest organizationUpsertRest = TestUtils.createOrganizationUpsert(SUBSCRIBER_ID, organizationName
                , null, dataRetentionDay);

        when(organizationRepository.findByIdAndSubscriberIdAndStatusIn(ORGANIZATION_ID, SUBSCRIBER_ID, Constants.ORGANIZATION_ACTIVE_STATUS)).thenReturn(organizationEntity);

        OrganizationBO orgBO = organizationService.updateOrganization(ORGANIZATION_ID, organizationUpsertRest, user);

        assertEquals(updatedStatus, orgBO.getStatus());
        assertNull(orgBO.getDeletionDate());
    }

}
