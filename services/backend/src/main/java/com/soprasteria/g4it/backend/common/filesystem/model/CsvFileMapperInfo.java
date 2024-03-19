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
        };
    }
}
