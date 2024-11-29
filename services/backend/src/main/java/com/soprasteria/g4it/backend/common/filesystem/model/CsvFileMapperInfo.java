/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.model;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Setter
@ConfigurationProperties(prefix = "filesystem.config.headers")
public class CsvFileMapperInfo implements FileMapperInfo {

    private List<Header> application;
    private List<Header> datacenter;
    private List<Header> equipementPhysique;
    private List<Header> equipementVirtuel;
    private List<Header> inventory;
    private List<Header> physicalEquipmentIndicator;
    private List<Header> virtualEquipmentIndicator;
    private List<Header> applicationIndicator;

    private List<Header> terminal;
    private List<Header> datacenterDigitalService;
    private List<Header> network;
    private List<Header> server;
    private List<Header> cloudInstance;
    private List<Header> physicalEquipmentIndicatorDigitalService;
    private List<Header> virtualMachine;
    private List<Header> virtualEquipmentIndicatorCloudInstance;
    private List<Header> virtualEquipmentCloudInstance;


    @Override
    public List<Header> getMapping(final FileType type) {
        return switch (type) {
            case UNKNOWN -> Collections.emptyList();
            case DATACENTER -> new ArrayList<>(List.copyOf(datacenter));
            case EQUIPEMENT_VIRTUEL -> new ArrayList<>(List.copyOf(equipementVirtuel));
            case EQUIPEMENT_PHYSIQUE -> new ArrayList<>(List.copyOf(equipementPhysique));
            case APPLICATION -> new ArrayList<>(List.copyOf(application));
            case PHYSICAL_EQUIPMENT_INDICATOR -> new ArrayList<>(physicalEquipmentIndicator);
            case VIRTUAL_EQUIPMENT_INDICATOR -> new ArrayList<>(virtualEquipmentIndicator);
            case APPLICATION_INDICATOR -> new ArrayList<>(applicationIndicator);
            case INVENTORY -> new ArrayList<>(List.copyOf(inventory));
            case DATACENTER_DIGITAL_SERVICE -> new ArrayList<>(List.copyOf(datacenterDigitalService));
            case PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE ->
                    new ArrayList<>(List.copyOf(physicalEquipmentIndicatorDigitalService));
            case NETWORK -> new ArrayList<>(List.copyOf(network));
            case SERVER -> new ArrayList<>(List.copyOf(server));
            case CLOUD_INSTANCE -> new ArrayList<>(List.copyOf(cloudInstance));
            case TERMINAL -> new ArrayList<>(List.copyOf(terminal));
            case VIRTUAL_MACHINE -> new ArrayList<>(List.copyOf(virtualMachine));
            case VIRTUAL_EQUIPMENT_INDICATOR_CLOUD_INSTANCE ->
                    new ArrayList<>(List.copyOf(virtualEquipmentIndicatorCloudInstance));
            case VIRTUAL_EQUIPMENT_CLOUD_INSTANCE -> new ArrayList<>(List.copyOf(virtualEquipmentCloudInstance));
        };
    }

    public Set<String> getHeaderFields(final FileType fileType, final boolean mandatory) {
        return getMapping(fileType).stream()
                .filter(h -> !mandatory || !h.isOptional())
                .map(Header::getName)
                .collect(Collectors.toSet());
    }
}
