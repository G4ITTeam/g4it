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
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserOrganizationRepository;
import com.soprasteria.g4it.backend.apiuser.repository.UserRoleOrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.business.FileSystem;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.OrganizationUpsertRest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Organization service.
 */
@Service
@Slf4j
public class OrganizationService {

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
     * Repository to manage user role.
     */
    @Autowired
    UserRoleOrganizationRepository userRoleOrganizationRepository;
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

    @Autowired
    private CacheManager cacheManager;

    @Value("${g4it.organization.deletion.day}")
    private Integer organizationDataDeletionDays;
    /**
     * The repository to access organization data.
     */
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private FileSystem fileSystem;

    /**
     * Retrieve the active Organization Entity.
     *
     * @param organizationId the organization id.
     * @return the organization.
     */
    @Cacheable("Organization")
    public Organization getOrganizationById(final Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new G4itRestException("404", String.format("organization %d not found", organizationId)));
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
     * @param subscriberId           the subscriber id.
     * @return organization BO.
     */
    @Transactional
    public OrganizationBO createOrganization(OrganizationUpsertRest organizationUpsertRest, UserBO user, Long subscriberId) {

        // Check if organization with same name already exist on this subscriber.
        organizationRepository.findBySubscriberIdAndName(organizationUpsertRest.getSubscriberId(), organizationUpsertRest.getName())
                .ifPresent(organization -> {
                    throw new G4itRestException("409", String.format("organization '%s' already exists in subscriber '%s'", organizationUpsertRest.getName(), subscriberId));
                });

        // Create organization
        final Organization organizationToCreate = organizationMapper.toEntity(
                organizationUpsertRest.getName(),
                subscriberService.getSubscriptionById(subscriberId),
                User.builder().id(user.getId()).build(),
                OrganizationStatus.ACTIVE.name()
        );
        organizationToCreate.setIsMigrated(true);
        organizationRepository.save(organizationToCreate);

        return organizationMapper.toBusinessObject(organizationToCreate);
    }

    /**
     * Update the organization.
     *
     * @param organizationUpsertRest the organizationUpsertRest.
     * @param userId                 the user id.
     * @return OrganizationBO
     */
    @Transactional
    public OrganizationBO updateOrganization(final Long organizationId, final OrganizationUpsertRest organizationUpsertRest, Long userId) {

        final Organization organizationToSave = getOrganizationByStatus(organizationUpsertRest.getSubscriberId(), organizationId, Constants.ORGANIZATION_ACTIVE_OR_DELETED_STATUS);

        final String currentStatus = organizationToSave.getStatus();
        final String newStatus = organizationUpsertRest.getStatus().name();

        if (currentStatus.equals(OrganizationStatus.ACTIVE.name()) && newStatus.equals(OrganizationStatus.ACTIVE.name())) {
            updateNameOrCriteria(organizationUpsertRest, organizationToSave);

        } else {
            Integer dataDeletionDays = null;
            LocalDateTime deletionDate = null;

            // Case current organization is ACTIVE and update it to TO_BE_DELETED
            if (currentStatus.equals(OrganizationStatus.ACTIVE.name()) && newStatus.equals(OrganizationStatus.TO_BE_DELETED.name())) {
                // Get data retention days
                dataDeletionDays = organizationUpsertRest.getDataRetentionDays() == null ?
                        organizationDataDeletionDays :
                        organizationUpsertRest.getDataRetentionDays().intValue();

                deletionDate = LocalDateTime.now().plusDays(dataDeletionDays.longValue());
            }

            organizationToSave.setDeletionDate(deletionDate);
            organizationToSave.setDataRetentionDay(dataDeletionDays);
            organizationToSave.setStorageRetentionDayExport(dataDeletionDays);
            organizationToSave.setStorageRetentionDayOutput(dataDeletionDays);
            organizationToSave.setStatus(organizationUpsertRest.getStatus().name());
        }
        organizationToSave.setLastUpdatedBy(User.builder()
                .id(userId)
                .build());
        organizationToSave.setLastUpdateDate(LocalDateTime.now());
        organizationRepository.save(organizationToSave);
        clearOrganizationCache(organizationId);
        return organizationMapper.toBusinessObject(organizationToSave);
    }

    /**
     * Update the organization's name or criteria
     *
     * @param organizationUpsertRest the organizationUpsertRest
     * @param organizationToSave     the updated organization
     */
    private void updateNameOrCriteria(OrganizationUpsertRest organizationUpsertRest, Organization organizationToSave) {
        final String currentOrganization = organizationToSave.getName();
        final String newOrganization = organizationUpsertRest.getName();

        final List<String> currentCriteriaDs = organizationToSave.getCriteriaDs();
        final List<String> newCriteriaDs = organizationUpsertRest.getCriteriaDs();

        final List<String> currentCriteriaIs = organizationToSave.getCriteriaIs();
        final List<String> newCriteriaIs = organizationUpsertRest.getCriteriaIs();
        boolean isCriteriaChange = !Objects.equals(newCriteriaDs, currentCriteriaDs) || !Objects.equals(newCriteriaIs, currentCriteriaIs);
        boolean isNameChange = !currentOrganization.equals(newOrganization);
        if (!(isCriteriaChange || isNameChange)) {
            log.info("Nothing to update in the organization '{}'", organizationToSave.getId());
            return;
        }
        // Set criteria for organization
        if (isCriteriaChange) {
            organizationToSave.setCriteriaDs(newCriteriaDs);
            organizationToSave.setCriteriaIs(newCriteriaIs);
        }
        if (isNameChange) {
            // Handle update in organization's name
            // Check if organization with same name already exist on this subscriber.
            organizationRepository.findBySubscriberIdAndName(organizationUpsertRest.getSubscriberId(), newOrganization)
                    .ifPresent(org -> {
                        throw new G4itRestException("409", String.format("organization '%s' already exists in subscriber '%s'", newOrganization, organizationUpsertRest.getSubscriberId()));
                    });

            log.info("Update Organization name in file system from '{}' to '{}'", currentOrganization, newOrganization);
            organizationToSave.setName(newOrganization);
        }
    }

    /**
     * clear cache to get the updated criteria
     */
    public void clearOrganizationCache(Long organizationId) {
        Objects.requireNonNull(cacheManager.getCache("Organization")).evict(organizationId);
    }
}
