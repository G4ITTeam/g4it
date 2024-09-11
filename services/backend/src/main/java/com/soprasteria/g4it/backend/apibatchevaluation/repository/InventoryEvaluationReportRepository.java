/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchevaluation.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Inventory Evaluation Report repository.
 */
@Repository
public interface InventoryEvaluationReportRepository extends JpaRepository<InventoryEvaluationReport, Long> {

    /**
     * Find by inventory identifier.
     *
     * @param inventoryId inventory identifier.
     * @return reports linked to inventory identifier.
     */
    List<InventoryEvaluationReport> findByInventoryId(final Long inventoryId);


    /**
     * Find by batch name
     *
     * @param batchName batch name
     * @return report
     */
    InventoryEvaluationReport findByBatchName(String batchName);

    /**
     * Find by batch status code
     *
     * @param batchStatusCode batch status code
     * @return list of report
     */
    List<InventoryEvaluationReport> findByBatchStatusCode(String batchStatusCode, Limit limit);

    List<InventoryEvaluationReport> findByBatchStatusCodeAndIsAggregated(String batchStatusCode, boolean isAggregated, Sort sort);

    @Modifying
    @Transactional
    @Query("""
            UPDATE InventoryEvaluationReport
            SET isAggregated = true
            WHERE id IN :ids
            """)
    int updateIsAggregated(@Param("ids") List<Long> ids);

    @Modifying
    @Transactional
    @Query("""
            UPDATE InventoryEvaluationReport
            SET batchStatusCode = :batchStatusCode, progressPercentage = '80%'
            WHERE batchStatusCode = 'COMPLETED' AND isAggregated = false
            """)
    int resetReportsIfNotAggregated(@Param("batchStatusCode") String batchStatusCode);

}
