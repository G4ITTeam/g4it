/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.filesystem.model;

import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import java.util.Map;

@SuperBuilder
@Jacksonized
@AllArgsConstructor
public class FileDescription implements Serializable {
    private String name;
    private FileType type;
    private Map<String, String> metadata;

    public String getName() {
        return name;
    }

    public FileType getType() {
        return type;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setType(FileType type) {
        this.type = type;
    }

}
