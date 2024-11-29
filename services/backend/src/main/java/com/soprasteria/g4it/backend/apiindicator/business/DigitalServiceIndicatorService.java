/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.apiindicator.controller.DigitalServiceCloudIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceNetworkIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceServerIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceTerminalIndicatorBO;
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

    public List<DigitalServiceTerminalIndicatorBO> getDigitalServiceTerminalIndicators(final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceTerminalIndicators(uid);
    }

    public List<DigitalServiceNetworkIndicatorBO> getDigitalServiceNetworkIndicators(final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceNetworkIndicators(uid);
    }

    public List<DigitalServiceServerIndicatorBO> getDigitalServiceServerIndicators(final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceServerIndicators(uid);
    }

    public List<DigitalServiceCloudIndicatorBO> getDigitalServiceCloudIndicators(final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceCloudIndicators(uid);
    }
}
