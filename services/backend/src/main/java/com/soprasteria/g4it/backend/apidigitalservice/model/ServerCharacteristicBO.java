/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.Data;
import lombok.Getter;
import lombok.experimental.SuperBuilder;


@Data
@SuperBuilder
public class ServerCharacteristicBO {

    public enum Code {
        LIFESPAN("LIFESPAN"),
        VCPU("VCPU"),
        DISK("DISK"),
        ANNUAL_ELECTRICITY_CONSUMPTION("ANNUALELECTRICITYCONSUMPTION");

        @Getter
        final String value;

        Code(String value) {
            this.value = value;
        }

    }

    /**
     * Code.
     */
    private String code;

    /**
     * Value.
     */
    private Double value;
}
