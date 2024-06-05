/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.repository;

import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Export repository.
 */
@Repository
public interface ExportReportRepository extends JpaRepository<ExportReport, Long> {

    /**
     * Find by inventory identifier.
     *
     * @param inventoryId inventory identifier.
     * @return reports linked to inventory identifier.
     */
    List<ExportReport> findByInventoryId(final Long inventoryId);

    /**
     * Find by file name.
     *
     * @param fileName
     * @return reports linked to export file name.
     */
    Optional<ExportReport> findByExportFilename(final String fileName);

    /**
     * Find Export Report by Batch Name
     *
     * @param batchName
     * @return Optional<ExportReport>
     */
    Optional<ExportReport> findByBatchName(String batchName);
}
