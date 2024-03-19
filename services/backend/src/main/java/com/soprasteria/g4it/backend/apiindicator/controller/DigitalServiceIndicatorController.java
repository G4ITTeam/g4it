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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

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
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceIndicatorRest>> getDigitalServiceIndicatorRest(final String subscriber,
                                                                                            final String organization,
                                                                                            final String digitalServiceUid) {
        final List<DigitalServiceIndicatorBO> indicators = indicatorService.getDigitalServiceIndicators(organization, digitalServiceUid);
        return ResponseEntity.ok().body(this.digitalServiceIndicatorRestMapper.toDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceTerminalIndicatorRest>> getDigitalServiceTerminalsIndicatorRest(final String subscriber,
                                                                                                             final String organization,
                                                                                                             final String digitalServiceUid) {
        final List<DigitalServiceTerminalIndicatorBO> indicators = indicatorService.getDigitalServiceTerminalIndicators(organization, digitalServiceUid);
        return ResponseEntity.ok().body(this.digitalServiceTerminalIndicatorRestMapper.toDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceNetworkIndicatorRest>> getDigitalServiceNetworksIndicatorRest(final String subscriber,
                                                                                                           final String organization,
                                                                                                           final String digitalServiceUid) {
        final List<DigitalServiceNetworkIndicatorBO> indicators = indicatorService.getDigitalServiceNetworkIndicators(organization, digitalServiceUid);
        return ResponseEntity.ok().body(this.digitalServiceNetworkIndicatorRestMapper.toDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DigitalServiceServerIndicatorRest>> getDigitalServiceServersIndicatorRest(final String subscriber,
                                                                                                         final String organization,
                                                                                                         final String digitalServiceUid) {
        final List<DigitalServiceServerIndicatorBO> indicatorBO = indicatorService.getDigitalServiceServerIndicators(organization, digitalServiceUid);
        return ResponseEntity.ok(digitalServiceServerIndicatorRestMapper.toDto(indicatorBO));
    }
}
