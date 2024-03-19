/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchevaluation.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
