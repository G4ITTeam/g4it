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
 * Referential Criterion
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "ref_criterion")
public class Criterion implements Serializable {

    /**
     * Criterion code
     */
    @Id
    private String code;

    /**
     * Criterion label
     */
    private String label;

    /**
     * Criterion description
     */
    private String description;

    /**
     * Criterion unit
     */
    private String unit;


    public static String[] getCsvHeaders() {
        return new String[]{"code", "label", "description", "unit"};
    }

    public Object[] toCsvRecord() {
        return new Object[]{code, label, description, unit};
    }

}


