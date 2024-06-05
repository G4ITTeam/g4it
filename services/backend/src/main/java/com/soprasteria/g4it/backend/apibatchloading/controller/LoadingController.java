/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchloading.controller;

import com.soprasteria.g4it.backend.apibatchloading.business.InventoryLoadingService;
import com.soprasteria.g4it.backend.apibatchloading.mapper.FileDescriptionRestMapper;
import com.soprasteria.g4it.backend.apibatchloading.model.InventoryLoadingSession;
import com.soprasteria.g4it.backend.server.gen.api.InventoryLoadingApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * Inventory Loading Rest Service.
 */
@Service
@NoArgsConstructor
public class LoadingController implements InventoryLoadingApiDelegate {

    @Autowired
    private InventoryLoadingService inventoryLoadingService;

    @Autowired
    private FileDescriptionRestMapper fileDescriptionRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Long> launchLoadingBatch(final String subscriber,
                                                   final String organization,
                                                   final Long inventoryId,
                                                   final List<FileDescriptionRest> filesDescription,
                                                   final String lang) {
        final InventoryLoadingSession session = InventoryLoadingSession.builder()
                .files(fileDescriptionRestMapper.toBusinessObject(filesDescription))
                .sessionDate(new Date())
                .subscriber(subscriber)
                .organization(organization)
                .inventoryId(inventoryId)
                .locale(LocaleContextHolder.getLocale())
                .build();
        return ResponseEntity.accepted().body(inventoryLoadingService.launchLoadingBatchJob(session));
    }
}
