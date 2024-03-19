/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository.numecoeval;

import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.VirtualEquipmentIndicator;
import com.soprasteria.g4it.backend.apiindicator.modeldb.numecoeval.VirtualEquipmentIndicatorKey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository to access Virtual equipment indicators.
 */
@Repository
public interface VirtualEquipmentIndicatorRepository extends JpaRepository<VirtualEquipmentIndicator, VirtualEquipmentIndicatorKey> {

    /**
     * Find all virtual equipment indicator by batch name.
     *
     * @param batchName the batch name linked to indicators.
     * @param pageable  spring pageable object.
     * @return virtual equipment indicator page.
     */
    Page<VirtualEquipmentIndicator> findByBatchName(@Param("batchName") final String batchName, final Pageable pageable);

}
