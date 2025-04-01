/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceIndicatorService;
import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceIndicatorViewService;
import com.soprasteria.g4it.backend.apiindicator.model.DigitalServiceIndicatorBO;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceEquipmentIndicatorViewServiceTest {

    @Mock
    private DigitalServiceIndicatorViewService digitalServiceIndicatorViewService;

    @InjectMocks
    private DigitalServiceIndicatorService indicatorService;

    @Test
    void shouldGetDigitalServiceIndicators() {
        final String uid = "uid";

        when(digitalServiceIndicatorViewService.getDigitalServiceIndicators(uid)).thenReturn(List.of(DigitalServiceIndicatorBO.builder().build()));

        Assertions.assertThat(indicatorService.getDigitalServiceIndicators(uid)).hasSize(1);

        verify(digitalServiceIndicatorViewService, times(1)).getDigitalServiceIndicators(uid);

    }
    
}
