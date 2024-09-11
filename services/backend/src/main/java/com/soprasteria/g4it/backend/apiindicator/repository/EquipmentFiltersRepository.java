/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.Filters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_BATCH_NAME;


/**
 * Repository to calculate the filters.
 */
@Repository
public interface EquipmentFiltersRepository extends JpaRepository<Filters, Long> {

    /**
     * Recovery of datacenter indicators.
     *
     * @param batchName the batch name.
     * @return main indicators
     */
    @Query(nativeQuery = true, value = """
            select 0 as id, 'country' as field, array_agg(distinct country) as values from agg_equipment_indicator
            where batch_name = :batchName
            union
            select 1 as id,'entity' as field, array_agg(distinct entity) as values from agg_equipment_indicator
            where batch_name = :batchName
            union
            select 2 as id,'type' as field, array_agg(distinct equipment) as values from agg_equipment_indicator
            where batch_name = :batchName
            union
            select 3 as id, 'status' as field, array_agg(distinct status) as values from agg_equipment_indicator
            where batch_name = :batchName
            """)
    List<Filters> getFiltersByBatchName(@Param(PARAM_BATCH_NAME) final String batchName);

}
