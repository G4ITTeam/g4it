/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.business;

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

    /**
     * Get inventory indicators.
     *
     * @param organization the organization.
     * @param uid          the inventory date.
     * @return indicators.
     */
    public List<DigitalServiceIndicatorBO> getDigitalServiceIndicators(final String organization, final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceIndicators(organization, uid);
    }

    public List<DigitalServiceTerminalIndicatorBO> getDigitalServiceTerminalIndicators(final String organization, final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceTerminalIndicators(organization, uid);
    }

    public List<DigitalServiceNetworkIndicatorBO> getDigitalServiceNetworkIndicators(final String organization, final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceNetworkIndicators(organization, uid);
    }

    public List<DigitalServiceServerIndicatorBO> getDigitalServiceServerIndicators(final String organization, final String uid) {
        return digitalServiceIndicatorViewService.getDigitalServiceServerIndicators(organization, uid);
    }

}
