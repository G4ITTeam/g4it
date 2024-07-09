/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.PhysicalEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentLowImpactView;
import com.soprasteria.g4it.backend.apiindicator.repository.PhysicalEquipmentAvgAgeViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.PhysicalEquipmentLowImpactViewRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Physical Equipment indicator Service.
 */
@Service
@AllArgsConstructor
public class PhysicalEquipmentIndicatorService {

    /**
     * Repository to access to average age indicators data.
     */
    @Autowired
    private PhysicalEquipmentAvgAgeViewRepository physicalEquipmentAvgAgeViewRepository;

    /**
     * Repository to access to low impact indicators data.
     */
    @Autowired
    private PhysicalEquipmentLowImpactViewRepository physicalEquipmentLowImpactViewRepository;

    /**
     * Physical equipment indicators mapper.
     */
    @Autowired
    private PhysicalEquipmentIndicatorMapper physicalEquipmentIndicatorMapper;

    /**
     * NumEcoEval Referential service.
     */
    @Autowired
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

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
     * Retrieve average age indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return average age indicators.
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, final Long organizationId, final Long inventoryId) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);
        final var result = physicalEquipmentAvgAgeViewRepository.findPhysicalEquipmentAvgAgeIndicators(inventoryId).stream()
                .peek(indicator -> indicator.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getType())))
                .toList();

        return physicalEquipmentIndicatorMapper.physicalEquipmentAvgAgetoDto(result);
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
        inventoryService.getInventory(subscriber, organizationId, inventoryId);
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        final List<PhysicalEquipmentLowImpactView> indicators = physicalEquipmentLowImpactViewRepository.findPhysicalEquipmentLowImpactIndicatorsByOrgId(inventoryId);
        indicators.forEach(indicator -> {
                    indicator.setType(TypeUtils.getShortType(subscriber, linkedOrganization.getName(), indicator.getType()));
                    indicator.setLowImpact(numEcoEvalReferentialRemotingService.isLowImpact(indicator.getPaysUtilisation()));
                }
        );

        return physicalEquipmentIndicatorMapper
                .physicalEquipmentLowImpacttoDTO(indicators);
    }
}
