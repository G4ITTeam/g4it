/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.filesystem.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FileType {
    DATACENTER("DATACENTER"),

    EQUIPEMENT_PHYSIQUE("EQUIPEMENT_PHYSIQUE"),

    EQUIPEMENT_VIRTUEL("EQUIPEMENT_VIRTUEL"),

    APPLICATION("APPLICATION"),

    INVENTORY("INVENTORY"),

    PHYSICAL_EQUIPMENT_INDICATOR("PHYSICAL_EQUIPMENT_INDICATOR"),

    VIRTUAL_EQUIPMENT_INDICATOR("VIRTUAL_EQUIPMENT_INDICATOR"),

    APPLICATION_INDICATOR("APPLICATION_INDICATOR"),

    UNKNOWN("UNKNOWN");

    private String value;

    public static FileType fromValue(String value) {
        for (FileType b : FileType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
