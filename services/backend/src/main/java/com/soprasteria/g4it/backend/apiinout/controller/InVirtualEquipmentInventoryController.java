/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinout.controller;

import com.soprasteria.g4it.backend.apiinout.business.InVirtualEquipmentService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryInputsVirtualEquipmentApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InVirtualEquipmentRest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/**
 * Inventory Input Virtual Equipment Service.
 */
@Slf4j
@Service
@AllArgsConstructor
@Validated
public class InVirtualEquipmentInventoryController implements InventoryInputsVirtualEquipmentApiDelegate {

    /**
     * Service to access virtual equipment input data.
     */
    private InVirtualEquipmentService inVirtualEquipmentService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> postInventoryInputsVirtualEquipmentsRest(final String subscriber, final Long organization, final Long inventoryId, final InVirtualEquipmentRest inVirtualEquipmentRest) {
        return new ResponseEntity<>(inVirtualEquipmentService.createInVirtualEquipmentInventory(inventoryId, inVirtualEquipmentRest), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InVirtualEquipmentRest>> getInventoryInputsVirtualEquipmentsRest(String subscriber,
                                                                                                Long organization,
                                                                                                Long inventoryId) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.getByInventory(inventoryId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> getInventoryInputsVirtualEquipmentRest(String subscriber,
                                                                                         Long organization,
                                                                                         Long inventoryId,
                                                                                         Long id) {
        return ResponseEntity.ok().body(inVirtualEquipmentService.getByInventoryAndId(inventoryId, id));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventoryInputsVirtualEquipmentRest(final String subscriber, final Long organization, final Long inventoryId, final Long id) {
        inVirtualEquipmentService.deleteInVirtualEquipment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InVirtualEquipmentRest> putInventoryInputsVirtualEquipmentRest(final String subscriber,
                                                                                         final Long organization, final Long inventoryId, final Long id,
                                                                                         final InVirtualEquipmentRest inVirtualEquipmentRest) {
        return new ResponseEntity<>(inVirtualEquipmentService.updateInVirtualEquipment(inventoryId, id, inVirtualEquipmentRest), HttpStatus.OK);
    }
}
