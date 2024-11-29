/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.modeldb;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Referential LifecycleStep
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "ref_lifecycle_step")
public class LifecycleStep implements Serializable {

    /**
     * Lifecycle step code
     */
    @Id
    private String code;

    /**
     * Lifecycle step label
     */
    private String label;

    public static String[] getCsvHeaders() {
        return new String[]{"code", "label"};
    }


    public Object[] toCsvRecord() {
        return new Object[]{code, label};
    }
}
