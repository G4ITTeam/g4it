/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.config;

import com.soprasteria.g4it.backend.apibatchloading.steps.common.decider.WriteValidFileDecider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Decider configuration.
 */
@Configuration
public class WriteValidFileDeciderConfiguration {

    /**
     * Write valid file decider configuration.
     *
     * @param writeValidFile property indicating to write valid file or not.
     * @return the decider configured.
     */
    @Bean
    public WriteValidFileDecider writeValidFileDecider(@Value("${loading.batch.valid-data-writing-enable:false}") final boolean writeValidFile) {
        return new WriteValidFileDecider(writeValidFile);
    }
}
