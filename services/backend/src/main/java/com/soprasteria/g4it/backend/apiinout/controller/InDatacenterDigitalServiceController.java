/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.InDatacenterService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceInputsDatacenterApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InDatacenterRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * DigitalService Input Datacenter Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InDatacenterDigitalServiceController implements DigitalServiceInputsDatacenterApiDelegate {

    /**
     * Service to access datacenter input data.
     */
    private InDatacenterService inDatacenterService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> postDigitalServiceInputsDatacentersRest(final String subscriber, final Long organization, final String digitalServiceUid, final InDatacenterRest inDatacenterRest) {
        return new ResponseEntity<>(inDatacenterService.createInDatacenterDigitalService(digitalServiceUid, inDatacenterRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InDatacenterRest>> getDigitalServiceInputsDatacentersRest(String subscriber,
                                                                                         Long organization,
                                                                                         String digitalServiceUid) {
        return ResponseEntity.ok().body(inDatacenterService.getByDigitalService(digitalServiceUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> getDigitalServiceInputsDatacenterRest(String subscriber,
                                                                                  Long organization,
                                                                                  String digitalServiceUid,
                                                                                  Long id) {
        return ResponseEntity.ok().body(inDatacenterService.getByDigitalServiceAndId(digitalServiceUid, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceInputsDatacenterRest(final String subscriber, final Long organization, final String digitalServiceUid, final Long id) {
        inDatacenterService.deleteInDatacenter(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InDatacenterRest> putDigitalServiceInputsDatacenterRest(final String subscriber,
                                                                                  final Long organization, final String digitalServiceUid, final Long id,
                                                                                  final InDatacenterRest inDatacenterRest) {
        return new ResponseEntity<>(inDatacenterService.updateInDatacenter(digitalServiceUid, id, inDatacenterRest), HttpStatus.OK);
    }

}
