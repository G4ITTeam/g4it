/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Digital Service Indicator Service.
 */
@Service
public class DigitalServiceIndicatorService {

    /**
     * Digital Service Indicator business service.
     */
    @Autowired
    private DigitalServiceIndicatorViewService digitalServiceIndicatorViewService;


    public List<DigitalServiceIndicatorBO> getDigitalServiceIndicators(final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceIndicators(uid);
    }
    
}
