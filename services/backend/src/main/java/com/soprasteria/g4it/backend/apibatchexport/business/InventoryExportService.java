/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.business;

import com.soprasteria.g4it.backend.apibatchexport.exception.EvaluationNotFoundException;
import com.soprasteria.g4it.backend.apibatchexport.repository.ExportReportRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryEvaluationReportBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryExportReportBO;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;

/**
 * Inventory Service.
 */
@Slf4j
@Service
public class InventoryExportService {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ExportJobService exportJobService;

    @Autowired
    private ExportReportRepository exportReportRepository;

    @Autowired
    private OrganizationService organizationService;

    /**
     * Create an export request for the inventory
     *
     * @param subscriber     the subscriber.
     * @param inventoryId    Inventory id
     * @param organizationId the organizationId.
     */
    public void createExportRequest(final String subscriber,
                                    final Long organizationId,
                                    final Long inventoryId) {

        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);

        if (inventory.getIsNewArch()) {
            // skipped because already created in file storage
            return;
        }
        
        if (CollectionUtils.isEmpty(inventory.getEvaluationReports())
                || inventory.getEvaluationReports().stream().noneMatch(report -> "COMPLETED".equals(report.getBatchStatusCode()))) {
            throw new EvaluationNotFoundException();
        }
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final String batchName = inventory.getEvaluationReports().stream()
                .max(Comparator.comparing(InventoryEvaluationReportBO::getEndTime))
                .orElseThrow()
                .getBatchName();

        exportJobService.launchExport(subscriber, linkedOrganization.getName(), inventory.getId(), inventory.getName(),
                batchName, organizationId);
    }

    /**
     * Delete export batch job.
     *
     * @param inventoryId the inventory id.
     */
    public void deleteExportBatchJob(final Long inventoryId) {
        exportJobService.deleteJobInstances(inventoryId);
    }


    /**
     * Get the Export Report by Inventory Id
     *
     * @param subscriber     the subscriber
     * @param organizationId the organizationId
     * @param inventoryId    the inventory id
     * @return the export object
     */
    public InventoryExportReportBO getExportReportByInventoryId(final String subscriber,
                                                                final Long organizationId,
                                                                final Long inventoryId) {
        return inventoryService.getInventory(subscriber, organizationId, inventoryId).getExportReport();
    }


    /**
     * Update batch status code to 'REMOVED'
     *
     * @param fileName file name
     */
    @Transactional
    public void updateBatchStatusCodeToRemove(String fileName) {
        exportReportRepository.findByExportFilename(fileName).ifPresent(exportReport -> {
            if (exportReport.getStatusCode().equals(ExportBatchStatus.EXPORT_GENERATED.name())) {
                exportReport.setStatusCode(ExportBatchStatus.REMOVED.name());
                exportReportRepository.save(exportReport);
            }
        });
    }


    /**
     * Update batch status code
     *
     * @param batchName       batch name
     * @param batchStatusCode status code
     */
    @Transactional
    public void updateBatchStatusCode(String batchName, String batchStatusCode) {
        exportReportRepository.findByBatchName(batchName).ifPresent(exportReport -> {
            exportReport.setStatusCode(batchStatusCode);
            exportReportRepository.save(exportReport);
        });
    }

}
