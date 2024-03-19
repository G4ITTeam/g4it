/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiversion.controller;

import com.soprasteria.g4it.backend.apiversion.business.VersionService;
import com.soprasteria.g4it.backend.server.gen.api.VersionApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.VersionRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Version Controller.
 */
@Service
public class VersionController implements VersionApiDelegate {

    @Autowired
    private VersionService versionService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<VersionRest> getVersion() {
        return ResponseEntity.ok(versionService.getVersion());
    }
}
