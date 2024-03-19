/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.application.listener;

import com.soprasteria.g4it.backend.apiinventory.mapper.ApplicationMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Application;
import com.soprasteria.g4it.backend.apiinventory.repository.ApplicationRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Flag application listener.
 * Use to update when duplicate.
 */
@AllArgsConstructor
@Slf4j
public class FlagApplicationDataWriterListener extends StepListenerSupport<Application, Application> {

    /**
     * The repository to access application data.
     */
    private ApplicationRepository applicationRepository;

    /**
     * Application mapper.
     */
    private ApplicationMapper applicationMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSkipInWrite(final Application application, final Throwable exception) {
        if (exception instanceof DataIntegrityViolationException) {
            log.debug("Duplicate Application (name {}, vm name {}, environment type {}), update values...", application.getNomApplication(), application.getNomEquipementVirtuel(), application.getTypeEnvironnement());
            applicationRepository.findByInventoryIdAndNomApplicationAndTypeEnvironnementAndNomEquipementVirtuel(application.getInventoryId(),
                            application.getNomApplication(), application.getTypeEnvironnement(), application.getNomEquipementVirtuel())
                    .ifPresentOrElse(applicationDb -> update(application, applicationDb), () -> applicationRepository.save(application));
        } else {
            log.warn("Unexpected Exception... ", exception);
        }
    }

    /**
     * Method to update the application.
     *
     * @param source the application read.
     * @param target the application from database.
     */
    private void update(final Application source, final Application target) {
        applicationMapper.merge(target, source);
        applicationRepository.save(target);
    }

}
