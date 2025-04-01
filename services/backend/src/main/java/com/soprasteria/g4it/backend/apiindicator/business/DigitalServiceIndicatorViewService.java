/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceCloudIndicatorRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceServerIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.controller.DigitalServiceCloudImpactBO;
import com.soprasteria.g4it.backend.apiindicator.controller.DigitalServiceCloudIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.mapper.DigitalServiceIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.ImpactStepBO;
import com.soprasteria.g4it.backend.apiindicator.model.VirtualServerImpactBO;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceNetworkIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceServerIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceTerminalIndicatorRepository;
import com.soprasteria.g4it.backend.apiinout.modeldb.OutVirtualEquipment;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.apiinout.repository.OutVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.groupingBy;

/**
 * Digital Service Indicator service.
 */
@Service
public class DigitalServiceIndicatorViewService {

    /**
     * Repository to access digital service indicator data.
     */
    @Autowired
    private DigitalServiceIndicatorRepository digitalServiceIndicatorRepository;

    /**
     * Repository to access digital service terminal indicator data
     */
    @Autowired
    private DigitalServiceTerminalIndicatorRepository digitalServiceTerminalIndicatorRepository;

    /**
     * Repository to access digital service network indicator data
     */
    @Autowired
    private DigitalServiceNetworkIndicatorRepository digitalServiceNetworkIndicatorRepository;

    /**
     * Repository to access digital service network indicator data
     */
    @Autowired
    private DigitalServiceServerIndicatorRepository digitalServiceServerIndicatorRepository;

    /**
     * Digital service indicator mapper.
     */
    @Autowired
    private DigitalServiceIndicatorMapper digitalServiceIndicatorMapper;

    @Autowired
    private DigitalServiceCloudIndicatorRestMapper digitalServiceCloudIndicatorRestMapper;

    @Autowired
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;

    @Autowired
    private OutVirtualEquipmentRepository outVirtualEquipmentRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * Retrieve digital service indicator.
     *
     * @param uid the digital service uid.
     * @return indicator list.
     */
    public List<DigitalServiceIndicatorBO> getDigitalServiceIndicators(final String uid) {
        return digitalServiceIndicatorMapper.toDto(digitalServiceIndicatorRepository.findDigitalServiceIndicators(uid));
    }

    /**
     * Build the hosting efficiency value from mixElecQuartile and pue
     * Host Efficiency: take max between quartile and pue efficiency
     * returns 1 -> "Good"; 2 -> "Medium"; 3 -> "Bad".
     *
     * @param mixElecQuartile the mixElecQuartile
     * @param pue             the pue
     * @return Good, Medium or Bad
     */
    private String buildHostingEfficiency(final Integer mixElecQuartile, final Double pue) {

        final Integer quartileEfficiency = switch (mixElecQuartile) {
            case 1 -> 1;
            case 2, 3 -> 2;
            default -> 3;
        };

        // Pue efficiency (<1,5 -> 1; 1.5 => x =>2,5 -> 2; >2.5 -> 3).
        final int pueEfficiency;
        if (pue < 1.5) {
            pueEfficiency = 1;
        } else if (pue <= 2.5) {
            pueEfficiency = 2;
        } else {
            pueEfficiency = 3;
        }

        final Integer hostEfficiencyValue = Collections.max(List.of(quartileEfficiency, pueEfficiency));
        return switch (hostEfficiencyValue) {
            case 1 -> "Good";
            case 2 -> "Medium";
            default -> "Bad";
        };
    }

    /**
     * Map server impact steps.
     *
     * @param serverIndicators the indicator list.
     * @return server impact step list.
     */
    private List<ImpactStepBO> buildImpactStepList(final List<DigitalServiceServerIndicatorView> serverIndicators) {
        return serverIndicators
                .stream()
                // Group by Acv Step to sum sipValue.
                .collect(groupingBy(DigitalServiceServerIndicatorView::getLifecycleStep))
                .entrySet().stream().map(DigitalServiceIndicatorViewService::buildImpactStep)
                .toList();
    }

    /**
     * Build impact step.
     *
     * @param serverIndicators the complex map containing acv step in key and indicators in value.
     * @return the impact step.
     */
    private static ImpactStepBO buildImpactStep(
            final Map.Entry<String, List<DigitalServiceServerIndicatorView>> serverIndicators) {
        return ImpactStepBO
                .builder()
                .acvStep(serverIndicators.getKey())
                // Sum value by acv step.
                .sipValue(serverIndicators.getValue().stream().filter(ind -> ind.getSipValue() != null).mapToDouble(DigitalServiceServerIndicatorView::getSipValue).sum())
                .rawValue(serverIndicators.getValue().stream().filter(ind -> ind.getRawValue() != null).mapToDouble(DigitalServiceServerIndicatorView::getRawValue).sum())
                .unit(serverIndicators.getValue().getFirst().getUnit())
                .status(serverIndicators.getValue().getFirst().getStatus())
                .countValue(serverIndicators.getValue().stream().mapToLong(DigitalServiceServerIndicatorView::getCountValue).sum())
                .build();
    }

    /**
     * Create the server impact list, one element with OK, one element with ERREUR
     *
     * @param serverIndicators the server indicators
     * @param isSharedServer   if is shared server
     * @return the list of impact
     */
    List<VirtualServerImpactBO> createVirtualServerDataList(List<DigitalServiceServerIndicatorView> serverIndicators, boolean isSharedServer) {

        List<VirtualServerImpactBO> result = new ArrayList<>();

        List<DigitalServiceServerIndicatorView> okData = serverIndicators.stream().filter(entry -> Objects.equals(entry.getStatus(), "OK")).toList();
        if (!okData.isEmpty()) {
            DigitalServiceServerIndicatorView firstImpactOk = okData.getFirst();
            result.add(buildVirtualServerImpactBO(
                    isSharedServer ? firstImpactOk.getVmName() : firstImpactOk.getServerName(),
                    isSharedServer ? firstImpactOk.getQuantity() : 1,
                    okData.stream().filter(ind -> ind.getSipValue() != null).mapToDouble(DigitalServiceServerIndicatorView::getSipValue).sum(),
                    okData.stream().filter(ind -> ind.getRawValue() != null).mapToDouble(DigitalServiceServerIndicatorView::getRawValue).sum(),
                    firstImpactOk.getUnit(),
                    firstImpactOk.getStatus(),
                    okData.stream().mapToLong(DigitalServiceServerIndicatorView::getCountValue).sum()
            ));
        }

        List<DigitalServiceServerIndicatorView> errorData = serverIndicators.stream().filter(entry -> Objects.equals(entry.getStatus(), "ERREUR")).toList();
        if (!errorData.isEmpty()) {
            DigitalServiceServerIndicatorView firstImpactError = errorData.getFirst();
            result.add(buildVirtualServerImpactBO(
                    isSharedServer ? firstImpactError.getVmName() : firstImpactError.getServerName(),
                    isSharedServer ? firstImpactError.getQuantity() : 1,
                    null, null,
                    firstImpactError.getUnit(),
                    firstImpactError.getStatus(),
                    errorData.stream().mapToLong(DigitalServiceServerIndicatorView::getCountValue).sum()
            ));
        }

        return result;
    }

    /**
     * Build virtual server impact.
     *
     * @param name     the server name.
     * @param quantity the quantity of vm or disk
     * @param sipValue the sipValue of the vm or disk
     * @return the virtual server impact.
     */
    private VirtualServerImpactBO buildVirtualServerImpactBO(final String name, final Integer quantity,
                                                             final Double sipValue, final Double rawValue, final String unit, final String status, final Long countValue) {
        return VirtualServerImpactBO
                .builder()
                .name(name)
                .quantity(quantity)
                .sipValue(sipValue)
                .rawValue(rawValue)
                .unit(unit)
                .status(status)
                .countValue(countValue)
                .build();
    }

    /**
     * Retrieve digital service cloud indicator.
     *
     * @param uid the digital service uid.
     * @return cloud indicator list.
     */
    public List<DigitalServiceCloudIndicatorBO> getDigitalServiceCloudIndicators(final String uid) {
        return taskRepository.findByDigitalServiceUid(uid)
                .map(value -> outVirtualEquipmentRepository.findByTaskId(value.getId()).stream()
                        .collect(groupingBy(OutVirtualEquipment::getCriterion))
                        .entrySet().stream()
                        .map(indicator -> new DigitalServiceCloudIndicatorBO(
                                StringUtils.snakeToKebabCase(indicator.getKey()),
                                indicator.getValue().stream().map(this::buildDigitalServiceCloudImpactBO).toList()))
                        .toList())
                .orElseGet(List::of);
    }

    /**
     * Build digitalServiceCloudImpact from outVirtualEquipment
     * Manual mapping
     *
     * @param outVirtualEquipment the virtual equipment output
     * @return the impact
     */
    private DigitalServiceCloudImpactBO buildDigitalServiceCloudImpactBO(final OutVirtualEquipment outVirtualEquipment) {

        return DigitalServiceCloudImpactBO.builder()
                .acvStep(outVirtualEquipment.getLifecycleStep())
                .cloudProvider(outVirtualEquipment.getProvider())
                .country(outVirtualEquipment.getLocation())
                .countValue(outVirtualEquipment.getCountValue())
                .instanceType(outVirtualEquipment.getInstanceType())
                .rawValue(outVirtualEquipment.getUnitImpact())
                .sipValue(outVirtualEquipment.getPeopleEqImpact())
                .status("ERROR".equals(outVirtualEquipment.getStatusIndicator()) ? "ERREUR" : outVirtualEquipment.getStatusIndicator())
                .unit(outVirtualEquipment.getUnit())
                .averageUsage(outVirtualEquipment.getUsageDuration())
                .averageWorkLoad(outVirtualEquipment.getWorkload())
                .quantity(outVirtualEquipment.getQuantity())
                .build();
    }
}
