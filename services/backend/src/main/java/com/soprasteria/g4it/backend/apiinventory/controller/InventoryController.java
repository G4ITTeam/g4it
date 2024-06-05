/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.controller;

import com.soprasteria.g4it.backend.apiinventory.business.InventoryDeleteService;
import com.soprasteria.g4it.backend.apiinventory.business.InventoryService;
import com.soprasteria.g4it.backend.apiinventory.mapper.InventoryRestMapper;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryUpdateRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Inventory Service.
 */
@Slf4j
@Service
@NoArgsConstructor
public class InventoryController implements InventoryApiDelegate {

    /**
     * Service to access inventory data.
     */
    @Autowired
    private InventoryService inventoryService;

    /**
     * Service to delete inventory data.
     */
    @Autowired
    private InventoryDeleteService inventoryDeleteService;

    /**
     * InventoryRest Mapper
     */
    @Autowired
    private InventoryRestMapper inventoryRestMapper;

    /**
     * User Service
     */
    @Autowired
    UserService userService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<InventoryRest>> getInventories(final String subscriber, final String organization, final Long inventoryId) {
        return ResponseEntity.ok().body(inventoryRestMapper.toRest(this.inventoryService.getInventories(subscriber, organization, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventories(final String subscriber, final String organization) {
        log.info("Start Delete inventory for organization {}", organization);
        inventoryDeleteService.deleteInventories(subscriber, organization);
        log.info("End Delete inventory for organization {}", organization);
        return ResponseEntity.noContent().<Void>build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteInventory(final String subscriber,
                                                final String organization,
                                                final Long inventoryId) {
        log.info("Start Delete inventory {} - {}", organization, inventoryId);
        inventoryDeleteService.deleteInventory(subscriber, organization, inventoryId);
        log.info("End Delete inventory {} - {}", organization, inventoryId);
        return ResponseEntity.noContent().<Void>build();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InventoryRest> createInventory(final String subscriber,
                                                         final String organization,
                                                         final InventoryCreateRest inventoryCreateRest) {
        return new ResponseEntity<>(inventoryRestMapper.toDto(this.inventoryService.createInventory(subscriber, organization, inventoryCreateRest)), HttpStatus.CREATED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<InventoryRest> updateInventory(final String subscriber,
                                                         final String organization,
                                                         final InventoryUpdateRest inventoryUpdateRest) {
        return new ResponseEntity<>(inventoryRestMapper.toDto(this.inventoryService.updateInventory(subscriber, organization, inventoryUpdateRest, userService.getUserEntity())), HttpStatus.OK);
    }

}
