/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.InPhysicalEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceInputsPhysicalEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InPhysicalEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Digital Service Input Physical Equipment Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InPhysicalEquipmentDigitalServiceController implements DigitalServiceInputsPhysicalEquipmentApiDelegate {

    /**
     * Service to access physical equipment input data.
     */
    private InPhysicalEquipmentService inPhysicalEquipmentService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> postDigitalServiceInputsPhysicalEquipmentsRest(final String subscriber, final Long organization, final String digitalServiceUid, final InPhysicalEquipmentRest inPhysicalEquipmentRest) {
        return new ResponseEntity<>(inPhysicalEquipmentService.createInPhysicalEquipmentDigitalService(digitalServiceUid, inPhysicalEquipmentRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InPhysicalEquipmentRest>> getDigitalServiceInputsPhysicalEquipmentsRest(String subscriber,
                                                                                                       Long organization,
                                                                                                       String digitalServiceUid) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByDigitalService(digitalServiceUid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> getDigitalServiceInputsPhysicalEquipmentRest(String subscriber,
                                                                                                Long organization,
                                                                                                String digitalServiceUid,
                                                                                                Long id) {
        return ResponseEntity.ok().body(inPhysicalEquipmentService.getByDigitalServiceAndId(digitalServiceUid, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteDigitalServiceInputsPhysicalEquipmentRest(final String subscriber, final Long organization, final String digitalServiceUid, final Long id) {
        inPhysicalEquipmentService.deleteInPhysicalEquipment(digitalServiceUid, id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InPhysicalEquipmentRest> putDigitalServiceInputsPhysicalEquipmentRest(final String subscriber,
                                                                                                final Long organization, final String digitalServiceUid, final Long id,
                                                                                                final InPhysicalEquipmentRest inPhysicalEquipmentRest) {
        return new ResponseEntity<>(inPhysicalEquipmentService.updateInPhysicalEquipment(digitalServiceUid, id, inPhysicalEquipmentRest), HttpStatus.OK);
    }
}
