/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DatacenterDigitalService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Datacenter Digital Service Repository.
 */
@Repository
public interface DatacenterDigitalServiceRepository extends JpaRepository<DatacenterDigitalService, String> {

    /**
     * Find by digital service UID.
     *
     * @param digitalServiceUid the unique digital service identifier.
     * @return DatacenterDigitalService list.
     */
    List<DatacenterDigitalService> findByDigitalServiceUid(final String digitalServiceUid);

    /**
     * Find by unique identifier.
     *
     * @param datacenterUid the datacenter UID.
     * @return the datacenter
     */
    Optional<DatacenterDigitalService> findByUid(final String datacenterUid);

}
