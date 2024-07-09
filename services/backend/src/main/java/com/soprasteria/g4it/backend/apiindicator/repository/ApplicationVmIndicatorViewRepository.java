/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationVmIndicatorView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_BATCH_NAME;
import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_INVENTORY_ID;

/**
 * Repository to calculate the application vm indicators
 */
@Repository
public interface ApplicationVmIndicatorViewRepository extends JpaRepository<ApplicationVmIndicatorView, Long> {

    /**
     * Method to recovery of application vm indicators.
     *
     * @param batchName       the batch name.
     * @param inventoryid     the inventory id.
     * @param applicationName the application name.
     * @param criteria        the criteria's label.
     * @return main application indicators
     */
    @Query(nativeQuery = true)
    List<ApplicationVmIndicatorView> findIndicators(@Param(PARAM_BATCH_NAME) final String batchName,
                                                    @Param(PARAM_INVENTORY_ID) final Long inventoryid,
                                                    @Param("applicationName") final String applicationName,
                                                    @Param("criteria") final String criteria);

}
