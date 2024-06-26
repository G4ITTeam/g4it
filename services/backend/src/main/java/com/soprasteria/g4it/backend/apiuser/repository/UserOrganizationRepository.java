/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.UserOrganization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserOrganizationRepository extends JpaRepository<UserOrganization, Long> {

    /**
     * Find by organization and return  a list of matching UserOrganizations
     *
     * @param organization the linked organization
     * @return UserOrganization
     */
    List<UserOrganization> findByOrganization(final Organization organization);

    /**
     * Find by linked organizationId and userId  and return  a list of matching UserOrganizations
     *
     * @param organizationId the linked organization's id
     * @param userId         the linked user's id
     * @return UserOrganization
     */
    Optional<UserOrganization> findByOrganizationIdAndUserId(final Long organizationId, final Long userId);

}
