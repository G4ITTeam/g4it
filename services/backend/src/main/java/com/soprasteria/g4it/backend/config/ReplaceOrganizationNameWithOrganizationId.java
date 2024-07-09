/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.config;

import com.soprasteria.g4it.backend.apibatchexport.modeldb.ExportReport;
import com.soprasteria.g4it.backend.apibatchexport.repository.ExportReportRepository;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryIntegrationReport;
import com.soprasteria.g4it.backend.apiinventory.repository.IntegrationReportRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.repository.OrganizationRepository;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileSystem;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Service
@Profile("!test")
public class ReplaceOrganizationNameWithOrganizationId {

    @Autowired
    private IntegrationReportRepository integrationReportRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private ExportReportRepository exportReportRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private FileSystem fileSystem;
    @Autowired
    private NumEcoEvalRepository numEcoEvalRepository;

    /**
     * Execute after context refresh event to avoid hibernate lazy loading issue.
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) throws IOException {
        event.getApplicationContext().getBean(ReplaceOrganizationNameWithOrganizationId.class).replaceOrganizationName();
    }


    /**
     * Replace Organization name with Organization id in :
     * - g4it_export_report -> result_file_url
     * - g4it_integration_report -> export_filename
     * - azure storage.
     */
    @Transactional
    public void replaceOrganizationName() throws IOException {

        List<Organization> organizations = new ArrayList<>();
        organizations.addAll(organizationRepository.findAllByIsMigrated(false));
        organizations.addAll(organizationRepository.findAllByIsMigrated(null));

        if (organizations.isEmpty()) return;

        for (Organization organization : organizations) {
            final String subscriber = organization.getSubscriber().getName();
            final String organizationName = organization.getName();
            final long organizationId = organization.getId();

            log.info("{}/{} : Renaming to '{}/{}' in NumEcoEval en_equipement_physique table", subscriber, organizationName, subscriber, organizationId);

            List<Inventory> inventoriesByOrganization = inventoryRepository.findByOrganization(organization);

            inventoriesByOrganization.forEach(inventory -> {
                // Change File URLs in 'g4it_integration_report' table.
                inventory.getEvaluationReports().forEach(inventoryEvaluationReport -> {
                    log.info("{}/{} : Renaming to '{}/{}' in NumEcoEval en_equipement_physique table for batchName:{}", subscriber, organizationName, subscriber, organizationId, inventoryEvaluationReport.getBatchName());
                    numEcoEvalRepository.updateOrganizationNameToId(
                            inventoryEvaluationReport.getBatchName(),
                            organizationName,
                            String.valueOf(organizationId)
                    );
                });
            });
        }

        for (Organization organization : organizations) {
            final String subscriber = organization.getSubscriber().getName();
            final String organizationName = organization.getName();
            final long organizationId = organization.getId();

            log.info("{}/{} : Renaming to '{}/{}'", subscriber, organizationName, subscriber, organizationId);

            // Replace Organization name with Organization id in azure storage.
            try {
                final FileStorage fileStorage = fileSystem.mount(subscriber, organizationName);
                fileStorage.renameOrganization(String.valueOf(organizationId));
            } catch (FileAlreadyExistsException e) {
                log.info("{}/{} : Files already migrated", subscriber, organizationName);
            } catch (Exception e) {
                log.error("{}/{} : Cannot rename to '{}/{}'", subscriber, organizationName, subscriber, organizationId, e);
                throw e;
            }

            List<Inventory> inventoriesByOrganization = inventoryRepository.findByOrganization(organization);

            inventoriesByOrganization.forEach(inventory -> {

                // Change File URLs in 'g4it_integration_report' table.
                List<InventoryIntegrationReport> reports = inventory.getIntegrationReports().stream()
                        .filter(report -> !ObjectUtils.isEmpty(report.getResultFileUrl()))
                        .peek(report -> {
                            String fileUrl = report.getResultFileUrl();
                            try {
                                String newFileUrl = fileUrl.replaceFirst(organizationName, String.valueOf(organizationId));
                                report.setResultFileUrl(newFileUrl);
                                log.info("{}/{} : Replace integration fileUrl from '{}' to '{}'", subscriber, organizationName, fileUrl, newFileUrl);
                            } catch (Exception e) {
                                log.error("{}/{} : Error while renaming integration fileUrl '{}' for inventory '{}'", subscriber, organizationName, fileUrl, inventory.getId(), e);
                            }
                        })
                        .toList();

                integrationReportRepository.saveAll(reports);

                // Change File URLs in 'g4it_export_report' table.
                ExportReport exportReport = inventory.getExportReport();
                if (exportReport != null) {
                    String fileUrl = exportReport.getExportFilename();
                    if (!ObjectUtils.isEmpty(fileUrl)) {
                        try {
                            String newFileUrl = fileUrl.replaceFirst(organizationName, String.valueOf(organizationId));
                            exportReport.setExportFilename(newFileUrl);
                            log.info("{}/{} : Replace exportFile fileUrl from '{}' to '{}'", subscriber, organizationName, fileUrl, newFileUrl);

                        } catch (Exception e) {
                            log.error("{}/{} : Error while renaming exportFile fileUrl '{}' for inventory '{}'", subscriber, organizationName, fileUrl, inventory.getId(), e);
                        }
                        exportReportRepository.save(exportReport);
                    }
                }
            });

            organization.setIsMigrated(true);
            organizationRepository.save(organization);
        }

    }

}
