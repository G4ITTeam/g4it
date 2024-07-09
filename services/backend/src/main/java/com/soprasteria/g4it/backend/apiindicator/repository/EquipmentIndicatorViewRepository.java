/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentIndicatorView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_BATCH_NAME;

/**
 * Repository to calculate the indicators
 */
@Repository
public interface EquipmentIndicatorViewRepository extends JpaRepository<EquipmentIndicatorView, Long> {

    /**
     * method to recovery of main indicators.
     *
     * @param batchName the batch name.
     * @return main indicators
     */
    @Query(nativeQuery = true)
    List<EquipmentIndicatorView> findIndicators(@Param(PARAM_BATCH_NAME) final String batchName);


}
