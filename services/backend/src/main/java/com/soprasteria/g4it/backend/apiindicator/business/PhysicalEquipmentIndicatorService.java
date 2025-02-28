/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.PhysicalEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentElecConsumptionBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.*;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Physical Equipment indicator Service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class PhysicalEquipmentIndicatorService {

    /**
     * Repository to access to average age indicators data.
     */
    @Autowired
    private PhysicalEquipmentAvgAgeViewRepository physicalEquipmentAvgAgeViewRepository;
    @Autowired
    private InPhysicalEquipmentAvgAgeViewRepository inPhysicalEquipmentAvgAgeViewRepository;

    /**
     * Repository to access to low impact indicators data.
     */
    @Autowired
    private PhysicalEquipmentLowImpactViewRepository physicalEquipmentLowImpactViewRepository;

    @Autowired
    private InPhysicalEquipmentLowImpactViewRepository inPhysicalEquipmentLowImpactViewRepository;

    @Autowired
    private PhysicalEquipmentElecConsumptionViewRepository physicalEquipmentElecConsumptionViewRepository;

    @Autowired
    private InPhysicalEquipmentElecConsumptionViewRepository inPhysicalEquipmentElecConsumptionViewRepository;

    /**
     * Physical equipment indicators mapper.
     */
    @Autowired
    private PhysicalEquipmentIndicatorMapper physicalEquipmentIndicatorMapper;

    /**
     * The Organization Service
     */
    @Autowired
    private OrganizationService organizationService;

    /**
     * The Inventory Service
     */
    @Autowired
    private InventoryService inventoryService;

    /**
     * The LowImpact Service
     */
    @Autowired
    private LowImpactService lowImpactService;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Retrieve average age indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return average age indicators.
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final InventoryBO inventoryBO = inventoryService.getInventory(subscriber, organizationId, inventoryId);

        if (Boolean.TRUE.equals(inventoryBO.getIsNewArch())) {
            Task task = taskRepository.findByInventoryAndLastCreationDate(Inventory.builder().id(inventoryId).build()).orElseThrow();
            var result = inPhysicalEquipmentAvgAgeViewRepository.findPhysicalEquipmentAvgAgeIndicators(task.getId());
            return physicalEquipmentIndicatorMapper.inPhysicalEquipmentAvgAgetoDto(result);
        } else {
            final var result = physicalEquipmentAvgAgeViewRepository.findPhysicalEquipmentAvgAgeIndicators(inventoryId).stream()
                    .peek(indicator -> indicator.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getType())))
                    .toList();

            return physicalEquipmentIndicatorMapper.physicalEquipmentAvgAgetoDto(result);
        }

    }


    /**
     * Retrieve low impact indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return low impact indicators.
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber, final Long organizationId, final Long inventoryId) {
        // check inventory is linked to subscriber and organizationId
        final InventoryBO inventory = inventoryService.getInventory(subscriber, organizationId, inventoryId);
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        if (Boolean.TRUE.equals(inventory.getIsNewArch())) {
            final List<InPhysicalEquipmentLowImpactView> indicators = inPhysicalEquipmentLowImpactViewRepository.findPhysicalEquipmentLowImpactIndicatorsByOrgId(inventoryId);
            indicators.forEach(indicator -> {
                        indicator.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getType()));
                        indicator.setLowImpact(lowImpactService.isLowImpact(indicator.getCountry()));
                    }
            );
            return physicalEquipmentIndicatorMapper.inPhysicalEquipmentLowImpacttoDTO(indicators);
        } else {
            final List<PhysicalEquipmentLowImpactView> indicators = physicalEquipmentLowImpactViewRepository.findPhysicalEquipmentLowImpactIndicatorsByOrgId(inventoryId);
            indicators.forEach(indicator -> {
                        indicator.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getType()));
                        indicator.setLowImpact(lowImpactService.isLowImpact(indicator.getCountry()));
                    }
            );
            return physicalEquipmentIndicatorMapper.physicalEquipmentLowImpacttoDTO(indicators);
        }
    }

    /**
     * Retrieve electric consumption of physical equipments
     *
     * @param subscriber     the subscriber
     * @param organizationId the organization's id
     * @param batchName      the batch name
     * @return electric consumption indicators
     */
    public List<PhysicalEquipmentElecConsumptionBO> getPhysicalEquipmentElecConsumption(final String subscriber, final Long organizationId, final String batchName, final Long criteriaNumber) {

        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        // isNewArch mode
        if (StringUtils.isNumeric(batchName)) {
            final var indicators = inPhysicalEquipmentElecConsumptionViewRepository.findPhysicalEquipmentElecConsumptionIndicators(Long.parseLong(batchName), criteriaNumber);
            return physicalEquipmentIndicatorMapper.inPhysicalEquipmentElecConsumptionToDto(indicators);
        } else {
            final List<PhysicalEquipmentElecConsumptionView> indicators =
                    physicalEquipmentElecConsumptionViewRepository.findPhysicalEquipmentElecConsumptionIndicators(batchName, criteriaNumber);

            indicators.forEach(
                    indicator -> indicator.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getType()))
            );

            return physicalEquipmentIndicatorMapper.physicalEquipmentElecConsumptionToDto(indicators);
        }
    }
}
