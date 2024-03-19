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
     * Find by organization name and username.
     *
     * @param organization the linked organization.
     * @param username     the userName to find.
     * @return DigitalService list.
     */
    List<DigitalService> findByOrganizationAndUserUsername(final Organization organization, final String username);

    /**
     * Verify if the digitalService exists by the uid and username.
     *
     * @param uid      the uid.
     * @param username the userName to find.
     * @return the digitalService.
     */
    @Cacheable("existsByUidAndUserUsername")
    boolean existsByUidAndUserUsername(final String uid, final String username);

}
