/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.business;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Organization service.
 */
@Service
@AllArgsConstructor
public class OrganizationService {

    /**
     * The repository to access organization data.
     */
    @Autowired
    private OrganizationRepository organizationRepository;

    /**
     * Retrieve the organization by name and subscriber name.
     *
     * @param subscriberName   the client subscriber name.
     * @param organizationName the linked organization name.
     * @return the organization.
     */
    @Cacheable(value = "Organization", key = "{#subscriberName, #organizationName}")
    public Organization getOrganization(final String subscriberName, final String organizationName) {
        return organizationRepository.findBySubscriberNameAndName(subscriberName, organizationName).orElseThrow();
    }

}
