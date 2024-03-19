/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiinventory.business;

import com.soprasteria.g4it.backend.apiinventory.mapper.InventoryMapper;
import com.soprasteria.g4it.backend.apiinventory.model.AbstractReportBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryEvaluationReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiinventory.repository.PhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.utils.EvaluationBatchStatus;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalRemotingService;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryType;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.common.utils.Constants.COMPLETE_PROGRESS_PERCENTAGE;

/**
 * Inventory Service.
 */
@Service
@NoArgsConstructor
@Slf4j
public class InventoryService {

    /**
     * Repository to access inventory data.
     */
    @Autowired
    private InventoryRepository inventoryRepository;

    /**
     * Repository to count physical equipment.
     */
    @Autowired
    private PhysicalEquipmentRepository physicalEquipmentRepository;

    /**
     * The organization service.
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * Mapper for inventory data.
     */
    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private NumEcoEvalRemotingService numEcoEvalRemotingService;

    /**
     * Retrieve all inventory of an organization if inventoryId is null.
     * Filter on inventoryId if not null
     *
     * @param subscriberName   the client subscriber name.
     * @param organizationName the linked organization name.
     * @param inventoryId      the inventory id optional query param
     * @return inventories BO.
     */
    public List<InventoryBO> getInventories(final String subscriberName, final String organizationName, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganization(subscriberName, organizationName);
        var inventories = inventoryId == null ?
                inventoryRepository.findByOrganization(linkedOrganization) :
                inventoryRepository.findById(inventoryId).stream().toList();
        /* Update calculation progress percentage */
        updateEvaluationProgressPercentage(new ArrayList<>(inventories));
        return inventoryMapper.toBusinessObject(inventories);
    }


    /**
     * Retrieving an inventory for an organization and inventory id.
     *
     * @param inventoryId the inventory id.
     * @return inventory BO.
     */
    public InventoryBO getInventory(final Long inventoryId) {
        final Inventory inventory = getInventoryEntity(inventoryId);
        final InventoryBO inventoryBo = inventoryMapper.toBusinessObject(inventory);
        Optional.ofNullable(inventoryBo.getIntegrationReports()).orElse(new ArrayList<>())
                .stream()
                .filter(report -> BatchStatus.STARTED.name().equals(report.getBatchStatusCode()))
                .findFirst()
                .ifPresent(report -> updateCount(inventory, inventoryBo));
        return inventoryBo;
    }

    /**
     * /**
     * Retrieve an inventory for an organization and inventory (without count)
     *
     * @param inventoryId the inventory id.
     * @return inventory BO.
     */
    public InventoryBO getInventoryLight(final Long inventoryId) {
        return inventoryMapper.toLightBusinessObject(getInventoryEntity(inventoryId));
    }


    /**
     * Create an inventory.
     *
     * @param subscriberName      the client subscriber name.
     * @param organizationName    the linked organization name.
     * @param inventoryCreateRest the inventoryCreateRest.
     * @return inventory BO.
     */
    public InventoryBO createInventory(final String subscriberName, final String organizationName, final InventoryCreateRest inventoryCreateRest) {
        final Organization linkedOrganization = organizationService.getOrganization(subscriberName, organizationName);

        if (inventoryRepository.findByOrganizationAndName(linkedOrganization, inventoryCreateRest.getName()).isPresent()) {
            throw new G4itRestException("409");
        }

        final Inventory inventoryToCreate = inventoryMapper.toEntity(linkedOrganization, inventoryCreateRest.getName(), inventoryCreateRest.getType().name());

        // Deprecated inventoryDate, used to better revert if needed
        if (InventoryType.INFORMATION_SYSTEM.name().equals(inventoryCreateRest.getType().name())) {
            inventoryToCreate.setInventoryDate(inventoryCreateRest.getName());
        }

        inventoryRepository.save(inventoryToCreate);
        return inventoryMapper.toCreateBusinessObject(inventoryToCreate);
    }

    /**
     * Update count values.
     *
     * @param inventory         the inventory containing updated data.
     * @param inventoryToUpdate the inventory business object to update.
     */
    private void updateCount(final Inventory inventory, final InventoryBO inventoryToUpdate) {
        inventoryToUpdate.setDataCenterCount((long) Hibernate.size(inventory.getDataCenterList()));
        inventoryToUpdate.setPhysicalEquipmentCount(physicalEquipmentRepository.countByInventoryId(inventory.getId()));
        inventoryToUpdate.setVirtualEquipmentCount((long) Hibernate.size(inventory.getVirtualEquipments()));
        inventoryToUpdate.setApplicationCount((long) Hibernate.size(inventory.getApplications()));
    }


    /**
     * Retrieve the last batch name in inventory.
     *
     * @param inventory the inventory.
     * @return the inventory's last batch name if present, or else empty.
     */
    public Optional<String> getLastBatchName(final InventoryBO inventory) {
        return Optional.ofNullable(inventory.getEvaluationReports()).orElse(new ArrayList<>())
                .stream()
                .filter(report -> "COMPLETED".equals(report.getBatchStatusCode()) && report.getEndTime() != null)
                .max(Comparator.comparing(InventoryEvaluationReportBO::getEndTime)).map(AbstractReportBO::getBatchName);
    }


    /**
     * Update the calculation progress percentage for evaluation report
     *
     * @param inventories the inventory.
     */
    private void updateEvaluationProgressPercentage(final List<Inventory> inventories) {
        List<Inventory> inventoriesToBeUpdated = new ArrayList<>();
        inventories.forEach(inventory -> Optional.ofNullable(inventory.getEvaluationReports()).orElse(new ArrayList<>())
                .stream()
                .filter(report -> EvaluationBatchStatus.CALCUL_IN_PROGRESS.name().equals(report.getBatchStatusCode()))
                .forEach(report -> {
                    String calculProgressPercentage = numEcoEvalRemotingService.getCalculationsProgress(report.getBatchName(), inventory.getOrganization().getName());
                    if (calculProgressPercentage == null) return;

                    report.setProgressPercentage(calculProgressPercentage);
                    log.info("Updating calculation progress percentage to '{}' for inventory : {} ", calculProgressPercentage, inventory);
                    if (COMPLETE_PROGRESS_PERCENTAGE.equals(calculProgressPercentage)) {
                        report.setBatchStatusCode(BatchStatus.COMPLETED.name());
                        log.info("Updating batch status to 'COMPLETED' of batch : '{}' ", report.getBatchName());
                    }
                    inventoriesToBeUpdated.add(inventory);
                }));


        inventoryRepository.saveAll(inventoriesToBeUpdated);
    }

    /**
     * Get inventory entity.
     *
     * @param inventoryId the inventory Id.
     * @return inventory or else throw inventory not found.
     */
    private Inventory getInventoryEntity(final Long inventoryId) {
        return this.inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new G4itRestException("404"));
    }
}


