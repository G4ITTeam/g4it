/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Digital Service repository.
 */
@Repository
public interface DigitalServiceRepository extends JpaRepository<DigitalService, String> {

    /**
     * Find by organization name
     *
     * @param organization the linked organization.
     * @return DigitalService list.
     */
    List<DigitalService> findByOrganization(final Organization organization);

    /**
     * Find by organization name and userId.
     *
     * @param organization the linked organization.
     * @param userId       the userId to find.
     * @return DigitalService list.
     */
    List<DigitalService> findByOrganizationAndUserId(final Organization organization, final long userId);

    /**
     * Verify if the digitalService exists by the uid and userId.
     *
     * @param uid    the uid.
     * @param userId the userId to find.
     * @return the digitalService.
     */
    @Cacheable("existsByUidAndUserId")
    boolean existsByUidAndUserId(final String uid, final long userId);

}
