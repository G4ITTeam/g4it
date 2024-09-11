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

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_APPLICATION_NAME;
import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_BATCH_NAME;

/**
 * Repository to calculate the application filters.
 */
@Repository
public interface ApplicationFiltersRepository extends JpaRepository<Filters, Long> {

    /**
     * Recovery of application indicator filters.
     *
     * @param batchName the batch name.
     * @return the application filters.
     */
    @Query(nativeQuery = true, value = """
            select 0 as id, 'environment' as field, array_agg(distinct environment) as values from agg_application_indicator
            where batch_name = :batchName
            union
            select 1 as id, 'life_cycle' as field, array_agg(distinct life_cycle) as values from agg_application_indicator
            where batch_name = :batchName
            union
            select 2 as id, 'type' as field, array_agg(distinct equipment_type) as values from agg_application_indicator
            where batch_name = :batchName
            union
            select 3 as id, 'domain' as field, array_agg(sub_domains) as values from (
            select concat( domain, '||', string_agg(distinct(sub_domain ), '##')) as sub_domains from agg_application_indicator
            where batch_name = :batchName
            group by domain) as _
            """)
    List<Filters> getFiltersByBatchName(@Param(PARAM_BATCH_NAME) final String batchName);

    /**
     * Recovery of application indicator filters.
     * Filtered by applicationName
     *
     * @param batchName       the batch name.
     * @param applicationName the application name.
     * @return the application filters.
     */
    @Query(nativeQuery = true, value = """
            select 0 as id, 'environment' as field, array_agg(distinct environment) as values from agg_application_indicator
            where batch_name = :batchName
            and application_name = :applicationName
            union
            select 1 as id, 'life_cycle' as field, array_agg(distinct life_cycle) as values from agg_application_indicator
            where batch_name = :batchName
            and application_name = :applicationName
            union
            select 2 as id, 'type' as field, array_agg(distinct equipment_type) as values from agg_application_indicator
            where batch_name = :batchName
            and application_name = :applicationName
            union
            select 3 as id, 'domain' as field, array_agg(sub_domains) as values from (
            select concat( domain, '||', string_agg(distinct(sub_domain ), '##')) as sub_domains from agg_application_indicator
            where batch_name = :batchName
            and application_name = :applicationName
            group by domain) as _
            """)
    List<Filters> getFiltersByBatchNameAndApplicationName(@Param(PARAM_BATCH_NAME) final String batchName,
                                                          @Param(PARAM_APPLICATION_NAME) final String applicationName);
}

