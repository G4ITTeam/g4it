/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application.processor;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import lombok.AllArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Application - Virtual Equipment Consistency check Processor.
 */
@AllArgsConstructor
public class ApplicationVirtualEquipmentConsistencyCheckProcessor implements ItemProcessor<Application, Application> {

    /**
     * Internationalization message.
     */
    private final MessageSource messageSource;

    /**
     * The locale to use.
     */
    private final Locale locale;

    /**
     * {@inheritDoc}
     */
    @Override
    public Application process(Application application) throws Exception {
        application.setMessage(messageSource.getMessage("equipementvirtuel.should.exist", new String[]{application.getNomEquipementVirtuel()}, locale));
        return application;
    }

}
