/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.controller;

import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceIndicatorRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceNetworkIndicatorRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceServerIndicatorRestMapper;
import com.soprasteria.g4it.backend.apidigitalservice.mapper.DigitalServiceTerminalIndicatorRestMapper;
import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceExportService;
import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceIndicatorService;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceNetworkIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceServerIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceTerminalIndicatorBO;
import com.soprasteria.g4it.backend.server.gen.api.DigitalServiceIndicatorApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceIndicatorRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceNetworkIndicatorRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceServerIndicatorRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.DigitalServiceTerminalIndicatorRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Digital Service Indicator Rest Service.
 */
@Service
public class DigitalServiceIndicatorController implements DigitalServiceIndicatorApiDelegate {

    /**
     * Digital Service Indicator service.
     */
    @Autowired
    private DigitalServiceIndicatorService indicatorService;


    /**
     * Indicator rest mapper.
     */
    @Autowired
    private DigitalServiceIndicatorRestMapper digitalServiceIndicatorRestMapper;

    /**
     * Indicator Terminal rest mapper.
     */
    @Autowired
    private DigitalServiceTerminalIndicatorRestMapper digitalServiceTerminalIndicatorRestMapper;

    /**
     * Indicator Network rest mapper.
     */
    @Autowired
    private DigitalServiceNetworkIndicatorRestMapper digitalServiceNetworkIndicatorRestMapper;

    /**
     * Indicator Network rest mapper.
     */
    @Autowired
    private DigitalServiceServerIndicatorRestMapper digitalServiceServerIndicatorRestMapper;

    /**
     * Export Service
     */
    @Autowired
    private DigitalServiceExportService exportService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceIndicatorRest>> getDigitalServiceIndicatorRest(final String subscriber,
                                                                                            final Long organization,
                                                                                            final String digitalServiceUid) {
        final List<DigitalServiceIndicatorBO> indicators = indicatorService.getDigitalServiceIndicators(digitalServiceUid);
        return ResponseEntity.ok().body(this.digitalServiceIndicatorRestMapper.toDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceTerminalIndicatorRest>> getDigitalServiceTerminalsIndicatorRest(final String subscriber,
                                                                                                             final Long organization,
                                                                                                             final String digitalServiceUid) {
        final List<DigitalServiceTerminalIndicatorBO> indicators = indicatorService.getDigitalServiceTerminalIndicators(digitalServiceUid);
        return ResponseEntity.ok().body(this.digitalServiceTerminalIndicatorRestMapper.toDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceNetworkIndicatorRest>> getDigitalServiceNetworksIndicatorRest(final String subscriber,
                                                                                                           final Long organization,
                                                                                                           final String digitalServiceUid) {
        final List<DigitalServiceNetworkIndicatorBO> indicators = indicatorService.getDigitalServiceNetworkIndicators(digitalServiceUid);
        return ResponseEntity.ok().body(this.digitalServiceNetworkIndicatorRestMapper.toDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceServerIndicatorRest>> getDigitalServiceServersIndicatorRest(final String subscriber,
                                                                                                         final Long organization,
                                                                                                         final String digitalServiceUid) {
        final List<DigitalServiceServerIndicatorBO> indicatorBO = indicatorService.getDigitalServiceServerIndicators(digitalServiceUid);
        return ResponseEntity.ok(digitalServiceServerIndicatorRestMapper.toDto(indicatorBO));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Resource> getDigitalServiceIndicatorsExportResult(String subscriber,
                                                                            Long organization,
                                                                            String digitalServiceUid) {
        try {
            InputStream inputStream = exportService.createFiles(digitalServiceUid, subscriber, organization);
            return ResponseEntity.ok(new InputStreamResource(inputStream));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error occurred while downloading file: " + e.getMessage());
        }
    }
}
