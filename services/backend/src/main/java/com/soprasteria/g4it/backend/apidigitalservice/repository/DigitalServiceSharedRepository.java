/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceShared;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DigitalServiceSharedRepository extends JpaRepository<DigitalServiceShared, Long> {
    /**
     * Verify if the digitalServiceShared exists by the uid and userId.
     *
     * @param digitalServiceUid the digital shared service uid.
     * @param userId            the userId to find.
     * @return the boolean.
     */
    boolean existsByDigitalServiceUidAndUserId(final String digitalServiceUid, final long userId);

    /**
     * @param digitalServiceUid the shared digital service uid.
     * @param userId            the userId to find.
     * @return DigitalServiceShared
     */
    Optional<DigitalServiceShared> findByDigitalServiceUidAndUserId(final String digitalServiceUid, final long userId);

    /**
     * Find by organization name and userId.
     *
     * @param organization the linked organization.
     * @param userId       the user to find.
     * @return DigitalServiceShared list.
     */
    List<DigitalServiceShared> findByOrganizationAndUserId(final Organization organization, final long userId);

    /**
     * Find by digitalService id
     *
     * @param digitalServiceUid the digital service uid.
     * @return DigitalServiceShared list
     */
    List<DigitalServiceShared> findByDigitalServiceUid(final String digitalServiceUid);

}

