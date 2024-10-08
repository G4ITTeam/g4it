/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.business;

import com.soprasteria.g4it.backend.apibatchevaluation.repository.InventoryEvaluationReportRepository;
import com.soprasteria.g4it.backend.apiinventory.mapper.InventoryMapper;
import com.soprasteria.g4it.backend.apiinventory.model.AbstractReportBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryEvaluationReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.modeldb.InventoryEvaluationReport;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.model.UserBO;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.apiuser.modeldb.User;
import com.soprasteria.g4it.backend.common.dbmodel.Note;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryType;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryUpdateRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private InventoryEvaluationReportRepository inventoryEvaluationReportRepository;

    /**
     * Retrieve the last batch name in inventory.
     *
     * @param inventory the inventory.
     * @return the inventory's last batch name if present, or else empty.
     */
    public Optional<String> getLastBatchName(final InventoryBO inventory) {
        if (inventory.getEvaluationReports() == null) return Optional.empty();

        return inventory.getEvaluationReports().stream()
                .filter(report -> "COMPLETED".equals(report.getBatchStatusCode()) && report.getEndTime() != null)
                .max(Comparator.comparing(InventoryEvaluationReportBO::getEndTime))
                .map(AbstractReportBO::getBatchName);
    }

    /**
     * Retrieve number of criteria
     *
     * @param batchName the batch name
     * @return number of criteria on which evaluation was done
     */
    public Long getCriteriaNumber(final String batchName) {
        InventoryEvaluationReport evaluationReport = inventoryEvaluationReportRepository.findByBatchName(batchName);
        return (long) evaluationReport.getCriteria().size();
    }

    /**
     * Retrieve all inventory of an organization if inventoryId is null.
     * Filter on inventoryId if not null
     *
     * @param subscriberName the client subscriber name.
     * @param organizationId the linked organization's id.
     * @param inventoryId    the inventory id optional query param
     * @return inventories BO.
     */
    public List<InventoryBO> getInventories(final String subscriberName, final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        var inventories = inventoryId == null ?
                inventoryRepository.findByOrganization(linkedOrganization) :
                inventoryRepository.findByOrganizationAndId(linkedOrganization, inventoryId).stream().toList();

        return inventoryMapper.toBusinessObject(inventories);
    }

    /**
     * Retrieving an inventory for an organization and inventory id.
     *
     * @param subscriberName subscriberName
     * @param organizationId organizationId
     * @param inventoryId    inventoryId
     * @return InventoryBO
     */
    public InventoryBO getInventory(final String subscriberName, final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final Optional<Inventory> inventory = inventoryRepository.findByOrganizationAndId(linkedOrganization, inventoryId);

        if (inventory.isEmpty())
            throw new G4itRestException("404", String.format("inventory %d not found in %s/%s", inventoryId, subscriberName, organizationId));

        return inventoryMapper.toBusinessObject(inventory.get());
    }

    /**
     * Create an inventory.
     *
     * @param subscriberName      the client subscriber name.
     * @param organizationId      the linked organization's id.
     * @param inventoryCreateRest the inventoryCreateRest.
     * @return inventory BO.
     */
    public InventoryBO createInventory(final String subscriberName, final Long organizationId, final InventoryCreateRest inventoryCreateRest) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        if (inventoryRepository.findByOrganizationAndName(linkedOrganization, inventoryCreateRest.getName()).isPresent()) {
            throw new G4itRestException("409", String.format("inventory %s already exists in %s/%s", inventoryCreateRest.getName(), subscriberName, organizationId));
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
     * Update note or criteria for an inventory.
     *
     * @param subscriberName      the subscriberName.
     * @param organizationId      the organization's id
     * @param inventoryUpdateRest the inventoryUpdateRest.
     * @param user                the user.
     * @return InventoryBO
     */
    public InventoryBO updateInventory(final String subscriberName, final Long organizationId, final InventoryUpdateRest inventoryUpdateRest, UserBO user) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final Optional<Inventory> inventory = inventoryRepository.findByOrganizationAndId(linkedOrganization, inventoryUpdateRest.getId());
        if (inventory.isEmpty())
            throw new G4itRestException("404", String.format("inventory %d not found in %s/%s", inventoryUpdateRest.getId(), subscriberName, organizationId));

        final Inventory inventoryToSave = inventory.get();
        inventoryToSave.setName(inventoryUpdateRest.getName());

        List<String> currentCriteria = inventoryToSave.getCriteria();
        List<String> newCriteria = inventoryUpdateRest.getCriteria();
        // Set criteria
        if (!Objects.equals(currentCriteria, newCriteria)) {
            inventoryToSave.setCriteria(newCriteria);
        }
        // Set note
        Note note = inventoryToSave.getNote();

        if (inventoryUpdateRest.getNote() == null) {
            note = null;
        } else {
            final User userEntity = User.builder().id(user.getId()).build();
            if (inventoryToSave.getNote() == null) {
                note = Note.builder()
                        .content(inventoryUpdateRest.getNote().getContent())
                        .createdBy(userEntity)
                        .build();
            } else {
                note.setContent(inventoryUpdateRest.getNote().getContent());
            }
            note.setLastUpdatedBy(userEntity);
        }
        inventoryToSave.setNote(note);

        inventoryRepository.save(inventoryToSave);
        return inventoryMapper.toBusinessObject(inventoryToSave);
    }

}
