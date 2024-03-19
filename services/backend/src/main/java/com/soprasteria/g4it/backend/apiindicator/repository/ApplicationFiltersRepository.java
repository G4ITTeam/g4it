/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationFilters;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.*;

/**
 * Repository to calculate the application filters.
 */
@Repository
public interface ApplicationFiltersRepository extends JpaRepository<ApplicationFilters, Long> {

    /**
     * Recovery of application indicator filters.
     *
     * @param inventoryId the inventory id.
     * @param batchName   the batch name.
     * @return the application filters.
     */
    @Query(nativeQuery = true)
    @Cacheable("getFiltersByBatchName")
    List<ApplicationFilters> getFiltersByBatchName(@Param(PARAM_INVENTORY_ID) final Long inventoryId,
                                                   @Param(PARAM_BATCH_NAME) final String batchName);

    /**
     * Recovery of application indicator filters.
     * Filtered by applicationName
     *
     * @param inventoryId     the inventory id.
     * @param batchName       the batch name.
     * @param applicationName the application name.
     * @return the application filters.
     */
    @Query(nativeQuery = true)
    List<ApplicationFilters> getFiltersByBatchNameAndApplicationName(@Param(PARAM_INVENTORY_ID) final Long inventoryId,
                                                                     @Param(PARAM_BATCH_NAME) final String batchName,
                                                                     @Param(PARAM_APPLICATION_NAME) final String applicationName);
}

