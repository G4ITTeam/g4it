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
     * Retrieve average age indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return average age indicators.
     */
    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, final String organization, final Long inventoryId) {
        return physicalEquipmentIndicatorMapper
                .physicalEquipmentAvgAgetoDto(physicalEquipmentAvgAgeViewRepository
                        .findPhysicalEquipmentAvgAgeIndicators(subscriber, organization, inventoryId));
    }

    /**
     * Retrieve low impact indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return low impact indicators.
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber, final String organization, final Long inventoryId) {
        final List<PhysicalEquipmentLowImpactView> indicators = physicalEquipmentLowImpactViewRepository.findPhysicalEquipmentLowImpactIndicators(subscriber, organization, inventoryId);

        indicators.forEach(indicator ->
                indicator.setLowImpact(numEcoEvalReferentialRemotingService.isLowImpact(indicator.getPaysUtilisation()))
        );
        
        return physicalEquipmentIndicatorMapper
                .physicalEquipmentLowImpacttoDTO(indicators);
    }
}
