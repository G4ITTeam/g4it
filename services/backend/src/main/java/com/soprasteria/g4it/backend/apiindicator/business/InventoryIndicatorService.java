/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apievaluating.business.asyncevaluatingservice.ExportService;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiinout.repository.InApplicationRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InDatacenterRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InPhysicalEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.common.task.business.TaskService;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Inventory Service.
 */
@Service
@NoArgsConstructor
public class InventoryIndicatorService {

    @Autowired
    ExportService exportService;
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private IndicatorService indicatorService;
    @Autowired
    private OrganizationService organizationService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private InDatacenterRepository inDatacenterRepository;
    @Autowired
    private InPhysicalEquipmentRepository inPhysicalEquipmentRepository;
    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @Autowired
    private InApplicationRepository inApplicationRepository;
    @Autowired
    private TaskService taskService;

    /**
     * Get last batch name in the inventory business object.
     *
     * @param inventory the inventory business object.
     * @return the last batch name or else throw exception.
     */
    private Long getLastTaskId(final InventoryBO inventory) {
        Task task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder()
                        .id(inventory.getId())
                        .build())
                .orElseThrow(() -> new G4itRestException("404", String.format("inventory %d has no batch executed", inventory.getId())));
        return task.getId();

    }

    /**
     * Get inventory indicators.
     * *
     * * @param subscriber    the subscriber.
     * * @param organizationId  the organizationId.
     * * @param inventoryId the inventory id.
     * * @return indicators.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        return indicatorService.getEquipmentIndicators(getLastTaskId(inventory));
    }

    /**
     * Get inventory application indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     * @return indicators.
     */

    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        return indicatorService.getApplicationIndicators(getLastTaskId(inventory));
    }

    /**
     * Delete inventory indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     */
    public void deleteIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        // clean all evaluating tasks
        taskService.deleteEvaluatingTasksByInventoryId(subscriber, organizationId, inventoryId);
    }

    /**
     * Get datacenter indicators.
     *
     * @param inventoryId the inventory id.
     * @return datacenter indicators
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final Long inventoryId) {
        return indicatorService.getDataCenterIndicators(inventoryId);
    }

    /**
     * Get physical equipment average age indicators.
     *
     * @param inventoryId the inventory id.
     * @return datacenter indicators
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentAvgAge(inventoryId);
    }

    /**
     * Get physical equipment low impact indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organizationId.
     * @param inventoryId    the inventory id.
     * @return indicators
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber, final Long organizationId, final Long inventoryId) {
        return indicatorService.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId);
    }

    /**
     * Get electric consumption of physical equipments
     *
     * @param subscriber     the subscriber
     * @param organizationId the organization's id
     * @param inventoryId    the inventory id
     * @return electric consumption indicators
     */
    public List<PhysicalEquipmentElecConsumptionBO> getPhysicalEquipmentElecConsumption(final String subscriber, final Long organizationId, final Long inventoryId) {
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        Long taskId = getLastTaskId(inventory);
        final Long criteriaNumber = inventoryService.getCriteriaNumber(taskId);
        return indicatorService.getPhysicalEquipmentElecConsumption(taskId, criteriaNumber);
    }
}
