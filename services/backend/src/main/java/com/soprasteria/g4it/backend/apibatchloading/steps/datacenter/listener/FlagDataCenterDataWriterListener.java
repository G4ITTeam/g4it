/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.datacenter.listener;

import com.soprasteria.g4it.backend.apiinventory.mapper.DataCenterMapper;
import com.soprasteria.g4it.backend.apiinventory.modeldb.DataCenter;
import com.soprasteria.g4it.backend.apiinventory.repository.DataCenterRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Flag datacenter listener.
 * Use to update when duplicate.
 */
@AllArgsConstructor
@Slf4j
public class FlagDataCenterDataWriterListener extends StepListenerSupport<DataCenter, DataCenter> {

    /**
     * The repository to access datacenter data.
     */
    private DataCenterRepository dataCenterRepository;

    /**
     * DataCenter mapper.
     */
    private DataCenterMapper dataCenterMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSkipInWrite(final DataCenter dataCenter, final Throwable exception) {
        if (exception instanceof DataIntegrityViolationException) {
            log.debug("Duplicate Datacenter {}, update values...", dataCenter.getNomCourtDatacenter());
            dataCenterRepository.findByInventoryIdAndNomCourtDatacenter(dataCenter.getInventoryId(), dataCenter.getNomCourtDatacenter())
                    .ifPresentOrElse(datacenterDb -> update(dataCenter, datacenterDb), () -> dataCenterRepository.save(dataCenter));
        } else {
            log.warn("Unexpected Exception... ", exception);
        }
    }

    /**
     * Method to update the datacenter.
     *
     * @param source the datacenter read.
     * @param target the datacenter from database.
     */
    private void update(final DataCenter source, final DataCenter target) {
        dataCenterMapper.merge(target, source);
        dataCenterRepository.save(target);
    }
}
