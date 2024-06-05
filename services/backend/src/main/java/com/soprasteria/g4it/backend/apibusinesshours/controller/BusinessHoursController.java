/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apibusinesshours.controller;

import com.soprasteria.g4it.backend.apibusinesshours.business.BusinessHoursService;
import com.soprasteria.g4it.backend.server.gen.api.BusinessHoursApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.BusinessHoursRest;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@NoArgsConstructor
public class BusinessHoursController implements BusinessHoursApiDelegate {

    @Autowired
    private BusinessHoursService service;

    @Override
    public ResponseEntity<List<BusinessHoursRest>> getBusinessHours() {
        return ResponseEntity.ok(service.getBusinessHours());
    }

}
