/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.mapper.OrganizationMapper;
import com.soprasteria.g4it.backend.apiuser.model.OrganizationBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.Subscriber;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.common.utils.Constants.ORGANIZATION_ACTIVE_STATUS;

/**
 * Organization service.
 */
@Service
@Slf4j
public class OrganizationService {

    @Value("${g4it.organization.deletion.day}")
    private Integer organizationDataDeletionDays;

    /**
     * The repository to access organization data.
     */
    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * Organization Mapper.
     */
    @Autowired
    OrganizationMapper organizationMapper;

    /**
     * Repository to manage user organization.
     */
    @Autowired
    UserOrganizationRepository userOrganizationRepository;

    /**
     * Retrieve the organization by name and subscriber name.
     *
     * @param subscriberName   the client subscriber's name.
     * @param organizationName the linked organization's name.
     * @return the organization.
     */
    //@Cacheable(value = "Organization", key = "{#subscriberName, #organizationName}")
    public Organization getOrganizationBySubNameAndName(final String subscriberName, final String organizationName) {
        return organizationRepository.findBySubscriberNameAndName(subscriberName, organizationName).orElseThrow();
    }

    /**
     * Retrieve the organization Entity.
     *
     * @param subscriberId   the client subscriber's id.
     * @param organizationId the organization's id.
     * @param status         the organization's statuses.
     * @return the organization.
     */
    public Organization getOrganizationByStatus(final Long subscriberId, final Long organizationId, List<String> status) {
        Optional<Organization> optOrg = subscriberId == null ?
                organizationRepository.findByIdAndStatusIn(organizationId, status) :
                organizationRepository.findByIdAndSubscriberIdAndStatusIn(organizationId, subscriberId, status);

        return optOrg.orElseThrow(
                () -> new G4itRestException("404", String.format("organization with id '%d' not found", organizationId))
        );
    }

    /**
     * Create an Organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param user                   the user.
     * @param subscriber             the subscriber.
     * @return organization BO.
     */
    public OrganizationBO createOrganization(OrganizationUpsertRest organizationUpsertRest, User user, Subscriber subscriber) {
        Long subscriberId = subscriber.getId();

        // Check if organization with same name already exist on this subscriber.
        final Optional<Organization> optOrganization = organizationRepository.findBySubscriberIdAndName(
                organizationUpsertRest.getSubscriberId(),
                organizationUpsertRest.getName()
        );

        if (optOrganization.isPresent()) {
            throw new G4itRestException("409", String.format("organization '%s' already exists in '%s'", organizationUpsertRest.getName(), subscriberId));
        }

        // create organization
        final Organization organizationToCreate = organizationMapper.toEntity(organizationUpsertRest.getName(), subscriber, user, OrganizationStatus.ACTIVE.name());
        organizationRepository.save(organizationToCreate);
        // Insert entry in "UserOrganization" table.
        UserOrganization userOrganization = UserOrganization.builder()
                .user(user)
                .organization(organizationToCreate)
                .defaultFlag(true)
                .build();
        // TODO - Assign all roles to this user for newly created organization
        userOrganizationRepository.save(userOrganization);
        return organizationMapper.toBusinessObject(organizationToCreate);
    }

    /**
     * Update the organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param user                   the user.
     * @return OrganizationBO
     */
    @Transactional
    public OrganizationBO updateOrganization(final Long organizationId, final OrganizationUpsertRest organizationUpsertRest, User user) {

        final Organization organizationToSave = getOrganizationByStatus(organizationUpsertRest.getSubscriberId(), organizationId, ORGANIZATION_ACTIVE_STATUS);

        if (ObjectUtils.isEmpty(organizationUpsertRest.getStatus())) {
            // If organization status to set 'ACTIVE' again from 'TO_BE_DELETED'
            if (organizationToSave.getStatus().equals(OrganizationStatus.TO_BE_DELETED.name())) {
                // set status to active and remove deletion date
                organizationToSave.setDeletionDate(null);
                organizationToSave.setDataRetentionDay(null);
                organizationToSave.setStorageRetentionDayExport(null);
                organizationToSave.setStorageRetentionDayOutput(null);
                organizationToSave.setStatus(OrganizationStatus.ACTIVE.name());
            } else {
                // Handle update in organization's name
                if (!organizationToSave.getName().equals(organizationUpsertRest.getName())) {
                    organizationToSave.setName(organizationUpsertRest.getName());
                } else {
                    throw new G4itRestException("404", String.format("nothing to update in the organization '%s' ", organizationId));
                }
            }
        } else {
            // If organization status to set 'TO_BE_DELETED'
            if (organizationUpsertRest.getStatus().name().equals(OrganizationStatus.TO_BE_DELETED.name()) && organizationToSave.getStatus().equals(OrganizationStatus.ACTIVE.name())) {

                // Get data retention days
                Integer dataDeletionDays = organizationUpsertRest.getDataRetentionDays() == null ?
                        organizationDataDeletionDays :
                        organizationUpsertRest.getDataRetentionDays().intValue();

                organizationToSave.setDeletionDate(LocalDateTime.now().plusDays(dataDeletionDays.longValue()));
                organizationToSave.setDataRetentionDay(dataDeletionDays);
                organizationToSave.setStorageRetentionDayExport(dataDeletionDays);
                organizationToSave.setStorageRetentionDayOutput(dataDeletionDays);
                organizationToSave.setStatus(organizationUpsertRest.getStatus().name());
            }
        }
        organizationToSave.setLastUpdatedBy(user);
        organizationToSave.setLastUpdateDate(LocalDateTime.now());
        organizationRepository.save(organizationToSave);
        return organizationMapper.toBusinessObject(organizationToSave);
    }

}
