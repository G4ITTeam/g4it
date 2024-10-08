/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.apiindicator.mapper.ApplicationIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.EquipmentIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiindicator.modeldb.AggApplicationIndicator;
import com.soprasteria.g4it.backend.apiindicator.modeldb.AggEquipmentIndicator;
import com.soprasteria.g4it.backend.apiindicator.repository.AggApplicationIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.AggEquipmentIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.TypeUtils;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils.transformCriteriaNameToCriteriaKey;


/**
 * Indicator Service.
 */
@Service
@AllArgsConstructor
@Slf4j
public class IndicatorService {

    @Autowired
    private AggEquipmentIndicatorRepository aggEquipmentIndicatorRepository;

    @Autowired
    private AggApplicationIndicatorRepository aggApplicationIndicatorRepository;

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
    private OrganizationService organizationService;


    /**
     * Retrieve equipment indicators.
     *
     * @param subscriber     the subscriber
     * @param organizationId the organizationId
     * @param batchName      the num-eco-eval batch name.
     * @return indicator by criteria.
     */
    public Map<String, EquipmentIndicatorBO> getEquipmentIndicators(final String subscriber, final Long organizationId, final String batchName) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        final Map<String, List<AggEquipmentIndicator>> aggIndicatorsMap = aggEquipmentIndicatorRepository.findByBatchName(batchName).stream()
                .peek(aggEquipmentIndicator -> aggEquipmentIndicator.setEquipment(
                        TypeUtils.getShortType(subscriber, linkedOrganization.getName(), aggEquipmentIndicator.getEquipment())
                ))
                .collect(Collectors.groupingBy(AggEquipmentIndicator::getCriteria));

        return aggIndicatorsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        map -> transformCriteriaNameToCriteriaKey(map.getKey()),
                        map -> equipmentIndicatorMapper.toDto(map.getValue())));
    }

    /**
     * Retrieve application indicators.
     *
     * @param subscriber     subscriber
     * @param organizationId organization id
     * @param batchName      the num-eco-eval batch name.
     * @return indicator by criteria.
     */
    public List<ApplicationIndicatorBO<ApplicationImpactBO>> getApplicationIndicators(final String subscriber, final Long organizationId, final String batchName) {
        final Organization linkedOrganization = organizationService.getOrganizationById(organizationId);

        final List<AggApplicationIndicator> aggApplicationIndicators = aggApplicationIndicatorRepository.findByBatchName(batchName).stream()
                .peek(aggApplicationIndicator ->
                        aggApplicationIndicator.setEquipmentType(
                                TypeUtils.getShortType(subscriber, linkedOrganization.getName(), aggApplicationIndicator.getEquipmentType())
                        ))
                .toList();

        return applicationIndicatorMapper.toDto(aggApplicationIndicators);
    }

    /**
     * Delete indicators.
     *
     * @param batchName the batch name.
     */
    public void deleteIndicators(final String batchName) {
        log.info("Deleting NumEcoEval inputs, indicators and aggregated indicators for batchName={}", batchName);
        NumEcoEvalRepository.EN_TABLES.forEach(table -> numEcoEvalRepository.deleteByBatchName(table, batchName));
        // removing all indicators
        NumEcoEvalRepository.IND_TABLES.forEach(table -> numEcoEvalRepository.deleteByBatchName(table, batchName));
        aggEquipmentIndicatorRepository.deleteByBatchName(batchName);
        aggApplicationIndicatorRepository.deleteByBatchName(batchName);
    }

    /**
     * Retrieve datacenter indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return datacenter indicators.
     */
    public List<DataCentersInformationBO> getDataCenterIndicators(final String subscriber, final Long organizationId, final Long inventoryId) {
        return dataCenterIndicatorService.getDataCenterIndicators(subscriber, organizationId, inventoryId);
    }

    /**
     * Retrieve average age indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return average age indicators.
     */

    public List<PhysicalEquipmentsAvgAgeBO> getPhysicalEquipmentAvgAge(final String subscriber, Long organizationId, final long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentAvgAge(subscriber, organizationId, inventoryId);
    }

    /**
     * Retrieve low impact indicators.
     *
     * @param subscriber     the subscriber.
     * @param organizationId the organization's id.
     * @param inventoryId    the inventory id.
     * @return low impact indicators.
     */
    public List<PhysicalEquipmentLowImpactBO> getPhysicalEquipmentsLowImpact(final String subscriber,
                                                                             final Long organizationId,
                                                                             final Long inventoryId) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentsLowImpact(subscriber, organizationId, inventoryId);
    }

    /**
     * Retrieve electric consumption of physical equipments
     *
     * @param subscriber     the subscriber
     * @param organizationId the organization's id
     * @param batchName      the batch name
     * @return electric consumption indicators
     */
    public List<PhysicalEquipmentElecConsumptionBO> getPhysicalEquipmentElecConsumption(final String subscriber,
                                                                                        final Long organizationId,
                                                                                        final String batchName, final Long criteriaNumber) {
        return physicalEquipmentIndicatorService.getPhysicalEquipmentElecConsumption(subscriber, organizationId, batchName, criteriaNumber);
    }

}
