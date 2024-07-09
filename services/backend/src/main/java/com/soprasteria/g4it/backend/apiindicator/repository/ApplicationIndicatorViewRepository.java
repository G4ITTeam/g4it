/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationIndicatorView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_BATCH_NAME;
import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_INVENTORY_ID;

/**
 * Repository to calculate the application indicators
 */
@Repository
public interface ApplicationIndicatorViewRepository extends JpaRepository<ApplicationIndicatorView, Long> {

    /**
     * Method to recovery of main application indicators.
     *
     * @param batchName   the batch name.
     * @param inventoryId the inventory id.
     * @return main application indicators
     */
    @Query(nativeQuery = true)
    List<ApplicationIndicatorView> findIndicators(@Param(PARAM_BATCH_NAME) final String batchName,
                                                  @Param(PARAM_INVENTORY_ID) final Long inventoryId);


}
