/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.apiindicator.mapper.ApplicationIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.ApplicationVmIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.EquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.repository.ApplicationIndicatorViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.ApplicationVmIndicatorViewRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.EquipmentIndicatorViewRepository;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils.transformCriteriaKeyToCriteriaName;
import static com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils.transformCriteriaNameToCriteriaKey;


/**
 * Indicator Service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class IndicatorService {


    @Autowired
    private EquipmentIndicatorViewRepository equipmentIndicatorViewRepository;

    @Autowired
    private ApplicationIndicatorViewRepository applicationIndicatorViewRepository;

    @Autowired
    private ApplicationVmIndicatorViewRepository applicationVmIndicatorViewRepository;

    @Autowired
    private DataCenterIndicatorService dataCenterIndicatorService;

    @Autowired
    private PhysicalEquipmentIndicatorService physicalEquipmentIndicatorService;

    @Autowired
    private NumEcoEvalRepository numEcoEvalRepository;

    @Autowired
    private EquipmentIndicatorMapper equipmentIndicatorMapper;

    @Autowired
    private ApplicationIndicatorMapper applicationIndicatorMapper;

    @Autowired
    private ApplicationVmIndicatorMapper applicationVmIndicatorMapper;

    /**
     * Retrieve equipment indicators.
     *
     * @param organization the organization name.
     * @param batchName    the num-eco-eval batch name.
     * @return indicator by criteria.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final String organization, final String batchName) {
        final List<EquipmentIndicatorView> indicators = equipmentIndicatorViewRepository.findIndicators(organization, batchName);
        final Map<String, List<EquipmentIndicatorView>> indicatorsMap = indicators.stream().collect(Collectors.groupingBy(EquipmentIndicatorView::getCriteria));
        return indicatorsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        map -> transformCriteriaNameToCriteriaKey(map.getKey()),
                        map -> equipmentIndicatorMapper.toDto(map.getValue())));
    }

    /**
     * Retrieve application indicators.
     *
     * @param organizationName the organization name.
     * @param batchName        the num-eco-eval batch name.
     * @return indicator by criteria.
     */
    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final String organizationName, final String batchName, Long inventoryId) {
        return applicationIndicatorMapper.toDto(applicationIndicatorViewRepository.findIndicators(organizationName, batchName, inventoryId));
    }

    /**
     * Retrieve application indicators.
     *
     * @param organizationName the organization name.
     * @param batchName        the num-eco-eval batch name.
     * @param inventoryId      the inventory id
     * @param applicationName  the application name.
     * @param criteria         the criteria key.
     * @return indicator by criteria.
     */
    public List<ApplicationIndicatorBO<ApplicationVmImpactBO>> getApplicationVmIndicators(final String organizationName, final String batchName,
                                                                                          final Long inventoryId,
                                                                                          final String applicationName, final String criteria) {
        return applicationVmIndicatorMapper.toDto(applicationVmIndicatorViewRepository
                .findIndicators(organizationName, batchName, inventoryId, applicationName, transformCriteriaKeyToCriteriaName(criteria)));
    }

    /**
     * Delete indicators.
     *
     * @param organization the organization.
     * @param batchName    the batch name.
     */
    public void deleteIndicators(final String organization, final String batchName) {
        log.info("Deleting NumEcoEval inputs and indicators for organization={} and batchName={}", organization, batchName);
        // removing all data entry for indicators
        NumEcoEvalRepository.EN_TABLES.forEach(table -> numEcoEvalRepository.deleteByOrganizationAndBatchName(table, organization, batchName));

        // removing all indicators
        NumEcoEvalRepository.IND_TABLES.forEach(table -> numEcoEvalRepository.deleteByOrganizationAndBatchName(table, organization, batchName));
    }

    /**
     * Retrieve datacenter indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return datacenter indicators.
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final String subscriber, final String organization, final Long inventoryId) {
        return dataCenterIndicatorService.getDataCenterIndicators(subscriber, organization, inventoryId);
    }

    /**
     * Retrieve average age indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return average age indicators.
     */

    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, final String organization, final long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentAvgAge(subscriber, organization, inventoryId);
    }

    /**
     * Retrieve low carbon indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization.
     * @param inventoryId  the inventory id.
     * @return low carbon indicators.
     */
    public List<PhysicalEquipmentLowCarbonBO> getPhysicalEquipmentLowCarbon(final String subscriber,
                                                                            final String organization,
                                                                            final Long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentLowCarbon(subscriber, organization, inventoryId);
    }

}
