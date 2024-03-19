/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceIndicatorView;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceNetworkIndicatorView;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceServerIndicatorView;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceTerminalIndicatorView;
import com.soprasteria.g4it.backend.apiindicator.mapper.DigitalServiceIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.DigitalServiceNetworkIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.mapper.DigitalServiceTerminalIndicatorMapper;
import com.soprasteria.g4it.backend.apiindicator.model.*;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceNetworkIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceServerIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.DigitalServiceTerminalIndicatorRepository;
import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    /**
     * Digital service terminal indicator mapper
     */
    @Autowired
    private DigitalServiceTerminalIndicatorMapper digitalServiceTerminalIndicatorMapper;

    /**
     * Digital service network indicator mapper
     */
    @Autowired
    private DigitalServiceNetworkIndicatorMapper digitalServiceNetworkIndicatorMapper;

    /**
     * NumEcoEval Referential service.
     */
    @Autowired
    private NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    /**
     * Retrieve digital service indicator.
     *
     * @param organization organization.
     * @param uid          the digital service uid.
     * @return indicator list.
     */
    public List<DigitalServiceIndicatorBO> getDigitalServiceIndicators(final String organization, final String uid) {
        final List<DigitalServiceIndicatorView> indicators = digitalServiceIndicatorRepository.findDigitalServiceIndicators(organization, uid);
        return digitalServiceIndicatorMapper.toDto(indicators);
    }

    public List<DigitalServiceTerminalIndicatorBO> getDigitalServiceTerminalIndicators(final String organization, final String uid) {
        final List<DigitalServiceTerminalIndicatorView> indicators = digitalServiceTerminalIndicatorRepository.findDigitalServiceTerminalIndicators(organization, uid);
        return digitalServiceTerminalIndicatorMapper.toDto(indicators);
    }

    public List<DigitalServiceNetworkIndicatorBO> getDigitalServiceNetworkIndicators(final String organization, final String uid) {
        final List<DigitalServiceNetworkIndicatorView> indicators = digitalServiceNetworkIndicatorRepository.findDigitalServiceNetworkIndicators(organization, uid);
        return digitalServiceNetworkIndicatorMapper.toDto(indicators);
    }

    /**
     * Get Server indicators.
     *
     * @param organization the user organization.
     * @param uid          unique digital service identifier.
     * @return server indicator list.
     */
    public List<DigitalServiceServerIndicatorBO> getDigitalServiceServerIndicators(final String organization, final String uid) {
        final List<DigitalServiceServerIndicatorView> indicators = digitalServiceServerIndicatorRepository.findDigitalServiceServerIndicators(organization, uid);
        return indicators.stream()
                // (criteria, ([serverType, mutualizationType], serverName, indicators)
                .collect(groupingBy(DigitalServiceServerIndicatorView::getCriteria,
                        groupingBy(indicator -> Pair.of(indicator.getType(), indicator.getMutualizationType()),
                                groupingBy(DigitalServiceServerIndicatorView::getServerName))))
                .entrySet().stream()
                .map(indicator -> {
                            // (criteria, ([serverType, mutualizationType], serverName, indicators)
                            final String criteria = CriteriaUtils.transformCriteriaNameToCriteriaKey(indicator.getKey());

                            return new DigitalServiceServerIndicatorBO(criteria, indicator.getValue().entrySet().stream().map(ind -> {
                                        final String serverType = ind.getKey().getKey();
                                        final String mutualizationType = ind.getKey().getValue();
                                        return new DigitalServiceServerImpactBO(serverType, mutualizationType, ind.getValue().entrySet().stream()
                                                .map(entry -> buildServer(entry, mutualizationType))
                                                .toList());
                                    }
                            ).toList());
                        }
                ).toList();
    }

    /**
     * Map server.
     *
     * @param indicator         the complex map containing server name in key, and indicators in value.
     * @param mutualizationType the mutalization type of the server.
     * @return the server impact.
     */
    private ServersImpactBO buildServer(
            final Map.Entry<String, List<DigitalServiceServerIndicatorView>> indicator, final String mutualizationType) {

        final Optional<DigitalServiceServerIndicatorView> firstIndicator = indicator.getValue().stream().findFirst();
        String hostingEfficiency = null;
        if (firstIndicator.isPresent()) {
            final Integer mixElecQuartile = numEcoEvalReferentialRemotingService.getMixElecQuartileIndex(firstIndicator.get().getCriteria(), firstIndicator.get().getCountry());
            hostingEfficiency = buildHostingEfficiency(mixElecQuartile, firstIndicator.get().getPue());
        }

        return ServersImpactBO
                .builder()
                .name(indicator.getKey())
                // Sum sip value for each server's indicator.
                .totalSipValue(indicator.getValue().stream().mapToDouble(DigitalServiceServerIndicatorView::getSipValue).sum())
                .impactStep(buildImpactStepList(indicator.getValue()))
                .impactVmDisk(buildVirtualServerList(indicator.getValue(), mutualizationType))
                .hostingEfficiency(hostingEfficiency)
                .build();
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
     * @param value the indicator list.
     * @return server impact step list.
     */
    private List<ImpactStepBO> buildImpactStepList(final List<DigitalServiceServerIndicatorView> value) {
        return value
                .stream()
                // Group by Acv Step to sum sipValue.
                .collect(groupingBy(DigitalServiceServerIndicatorView::getLifecycleStep))
                .entrySet().stream().map(DigitalServiceIndicatorViewService::buildImpactStep)
                .toList();
    }

    /**
     * Build impact step.
     *
     * @param indicator the complex map containing acv step in key and indicators in value.
     * @return the impact step.
     */
    private static ImpactStepBO buildImpactStep(
            final Map.Entry<String, List<DigitalServiceServerIndicatorView>> indicator) {
        return ImpactStepBO
                .builder()
                .acvStep(indicator.getKey())
                // Sum value by acv step.
                .sipValue(indicator.getValue().stream().mapToDouble(DigitalServiceServerIndicatorView::getSipValue).sum())
                .build();
    }

    /**
     * Map virtual server impacts.
     *
     * @param value             the indicator list.
     * @param mutualizationType the mutalization type of the server.
     * @return the virtual server impact list.
     */
    private List<VirtualServerImpactBO> buildVirtualServerList(
            final List<DigitalServiceServerIndicatorView> value, final String mutualizationType) {
        if (mutualizationType.equals("SHARED")) {
            final Map<String, List<DigitalServiceServerIndicatorView>> test = value.stream().collect(Collectors.groupingBy(DigitalServiceServerIndicatorView::getVmUid));
            return test.entrySet().stream().map(entry -> buildVirtualServerImpactBO(
                    entry.getValue().get(0).getVmName(),
                    entry.getValue().get(0).getQuantity(),
                    entry.getValue().stream().mapToDouble(DigitalServiceServerIndicatorView::getSipValue).sum()
            )).toList();

        } else {
            final Map<String, List<DigitalServiceServerIndicatorView>> test = value.stream().collect(Collectors.groupingBy(DigitalServiceServerIndicatorView::getServerName));
            return test.entrySet().stream().map(entry -> buildVirtualServerImpactBO(
                            entry.getValue().get(0).getServerName(),
                            1,
                            entry.getValue().stream().mapToDouble(DigitalServiceServerIndicatorView::getSipValue).sum()))
                    .toList();
        }
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
                                                             final Double sipValue) {
        return VirtualServerImpactBO
                .builder()
                .name(name)
                .quantity(quantity)
                .sipValue(sipValue)
                .build();
    }

}
