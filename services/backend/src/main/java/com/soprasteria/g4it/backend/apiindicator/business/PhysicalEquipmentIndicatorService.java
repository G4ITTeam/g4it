/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.mapper.PhysicalEquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentLowCarbonBO;
import com.soprasteria.g4it.backend.apiindicator.model.PhysicalEquipmentsAvgAgeBO;
import com.soprasteria.g4it.backend.apiindicator.repository.PhysicalEquipmentAvgAgeViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.PhysicalEquipmentLowCarbonViewRepository;
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
     * Repository to access to low carbon indicators data.
     */
    @Autowired
    private PhysicalEquipmentLowCarbonViewRepository physicalEquipmentLowCarbonViewRepository;

    /**
     * Physical equipment indicators mapper.
     */
    @Autowired
    private PhysicalEquipmentIndicatorMapper physicalEquipmentIndicatorMapper;

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
     * Retrieve low carbon indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return low carbon indicators.
     */
    public List<PhysicalEquipmentLowCarbonBO> getPhysicalEquipmentLowCarbon(final String subscriber, final String organization, final Long inventoryId) {
        return physicalEquipmentIndicatorMapper
                .physicalEquipmentLowCarbontoDTO(physicalEquipmentLowCarbonViewRepository
                        .findPhysicalEquipmentLowCarbonIndicators(subscriber, organization, inventoryId));
    }


}
