/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.controller;

import com.soprasteria.g4it.backend.apireferential.business.ReferentialGetAnalysisService;
import com.soprasteria.g4it.backend.apireferential.mapper.AnalysisTableRestMapper;
import com.soprasteria.g4it.backend.apireferential.model.AnalysisTableBO;
import com.soprasteria.g4it.backend.server.gen.api.ReferentialAnalysisApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.AnalysisTableRest;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Referential analysis endpoints.
 */
@Service
@NoArgsConstructor
public class ReferentialGetAnalysisController implements ReferentialAnalysisApiDelegate {

    @Autowired
    private ReferentialGetAnalysisService reGetAnalysisService;

    @Autowired
    private AnalysisTableRestMapper analysisTableRestMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<AnalysisTableRest>> getAnalysis() {
        List<AnalysisTableBO> analysisBo = reGetAnalysisService.getAnalysis();
        return new ResponseEntity<>(analysisTableRestMapper.toRest(analysisBo), HttpStatus.OK);
    }
}
