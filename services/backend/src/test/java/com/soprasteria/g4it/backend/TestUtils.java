/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend;


import com.soprasteria.g4it.backend.apidigitalservice.model.DigitalServiceBO;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.model.SubscriberBO;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.*;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.server.gen.api.dto.LinkUserRoleRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.UserRoleRest;

import java.time.LocalDateTime;
import java.util.List;

public class TestUtils {

    public static String SUBSCRIBER = "SUBSCRIBER";
    public static String ORGANIZATION = "ORGANIZATION";
    public static String EMAIL = "user.test@unitaire";
    public static String FIRSTNAME = "user";
    public static String LASTNAME = "test";
    public static String SUBJECT = "7L-269202gdjG0hJl";
    public static String ROLE = "ROLE";

    public static UserBO createUserBO(final List<String> userRoles) {
        return UserBO.builder()
                .id(1)
                .email(EMAIL)
                .subscribers(List.of(SubscriberBO.builder()
                        .name(SUBSCRIBER)
                        .organizations(List.of(OrganizationBO.builder()
                                .name(ORGANIZATION)
                                .roles(userRoles)
                                .build()))
                        .build()))
                .build();
    }

    public static Organization createOrganization() {
        return Organization.builder()
                .id(1)
                .name(ORGANIZATION)
                .status(OrganizationStatus.ACTIVE.name())
                .subscriber(Subscriber.builder()
                        .id(1)
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }

    public static Organization createOrganizationWithStatus(String status) {
        return Organization.builder()
                .name(ORGANIZATION)
                .status(status)
                .deletionDate(LocalDateTime.now())
                .subscriber(Subscriber.builder()
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }

    public static Organization createOrganization(Long subId, Long orgId, String status, LocalDateTime deletionDate) {
        return Organization.builder()
                .id(orgId)
                .name(ORGANIZATION)
                .status(status)
                .deletionDate(deletionDate)
                .subscriber(Subscriber.builder()
                        .id(subId)
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }

    public static Organization createToBeDeletedOrganization(String status, LocalDateTime deletionDate) {
        return Organization.builder()
                .name(ORGANIZATION)
                .status(OrganizationStatus.TO_BE_DELETED.name())
                .deletionDate(deletionDate)
                .subscriber(Subscriber.builder()
                        .name(SUBSCRIBER)
                        .build())
                .build();
    }


    public static DigitalService createDigitalService() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        final DigitalService digitalService = DigitalService.builder().uid(digitalServiceUid).name("name").lastUpdateDate(LocalDateTime.now()).build();
        return digitalService;
    }

    public static DigitalServiceBO createDigitalServiceBO() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        return DigitalServiceBO.builder().uid(digitalServiceUid).name("name").lastUpdateDate(LocalDateTime.now()).build();
    }

    public static User createUserWithRoleOnSub(Long subscriberId, List<Role> roles) {
        return User.builder().email(EMAIL)
                .userSubscribers(List.of(UserSubscriber.builder().defaultFlag(true).roles(roles).subscriber(Subscriber.builder().name(SUBSCRIBER).build()).build()))
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true).roles(List.of(Role.builder().name("ROLE_INVENTORY_READ").build())).organization(Organization.builder().name(ORGANIZATION).status(OrganizationStatus.ACTIVE.name())
                                .subscriber(Subscriber.builder().id(subscriberId).name(SUBSCRIBER).build()).build()).build())).build();
    }

    public static User createUser(List<UserSubscriber> userSubscribers, List<UserOrganization> userOrganizations) {
        return User.builder().email(EMAIL)
                .userSubscribers(userSubscribers)
                .userOrganizations(userOrganizations)
                .build();
    }

    public static Subscriber createSubscriber(Long SubscriberId) {
        return Subscriber.builder().id(SubscriberId).name(SUBSCRIBER).build();
    }

    public static UserOrganization createUserOrganization(Long organizationId, List<Role> roles, String status) {
        return UserOrganization
                .builder().defaultFlag(true).roles(roles).organization(createOrganizationWithStatus(status)).build();
    }

    public static UserOrganization createUserOrganization(Long subId, Long orgId, List<Role> roles, String status, LocalDateTime deletionDate, List<UserRoleOrganization> userRoleOrganization) {
        return UserOrganization
                .builder().defaultFlag(true).roles(roles).organization(createOrganization(subId, orgId, status, deletionDate)).UserRoleOrganization(userRoleOrganization).build();
    }

    public static UserSubscriber createUserSubscriber(Long subscriberId, List<Role> roles, List<UserRoleSubscriber> userRoleSubscriberList) {
        return UserSubscriber
                .builder().defaultFlag(true).roles(roles).subscriber(createSubscriber(subscriberId)).UserRoleSubscriber(userRoleSubscriberList).build();
    }

    public static OrganizationUpsertRest createOrganizationUpsert(Long subscriberId, String orgName
            , String orgStatus, Long dataRetentionDays) {

        com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationStatus status = null;
        if (orgStatus != null)
            status = com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationStatus.valueOf(orgStatus);

        return OrganizationUpsertRest.builder().subscriberId(subscriberId).name(orgName).status(status)
                .dataRetentionDays(dataRetentionDays).build();
    }

    public static UserRoleSubscriber createUserRoleSubscriber(Role role) {
        return UserRoleSubscriber.builder().roles(role).build();
    }

    public static UserRoleOrganization createUserRoleOrganization(Role role) {
        return UserRoleOrganization.builder().roles(role).build();
    }

    public static LinkUserRoleRest createLinkUserRoleRest(Long organizationId, List<UserRoleRest> userRoleRest) {

        return LinkUserRoleRest.builder().organizationId(organizationId).users(userRoleRest).build();
    }

    public static UserRoleRest createUserRoleRest(Long userId, List<String> roles) {
        return UserRoleRest.builder().userId(userId).roles(roles).build();
    }

    public static User createUserWithAdminRoleOnOrg() {
        long organizationId = 1L;
        List<Role> organizationAdminRole = List.of(Role.builder().name(Constants.ROLE_ORGANIZATION_ADMINISTRATOR).build());
        return User.builder().email(EMAIL)
                .userOrganizations(List.of(UserOrganization
                        .builder().defaultFlag(true)
                        .roles(organizationAdminRole).organization(Organization.builder().id(organizationId).name(ORGANIZATION).status(OrganizationStatus.ACTIVE.name())
                                .build()).build())).build();


    }

}
