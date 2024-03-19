/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.business;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class DigitalServiceIndicatorViewServiceTest {
    
    @InjectMocks
    private DigitalServiceIndicatorViewService service;

    @Test
    void testBuildHostingEfficiency() {
        Assertions.assertEquals("Good", ReflectionTestUtils.invokeMethod(service, "buildHostingEfficiency", 1, 1.2));
        Assertions.assertEquals("Medium", ReflectionTestUtils.invokeMethod(service, "buildHostingEfficiency", 2, 1.2));
        Assertions.assertEquals("Medium", ReflectionTestUtils.invokeMethod(service, "buildHostingEfficiency", 2, 2.5));
        Assertions.assertEquals("Medium", ReflectionTestUtils.invokeMethod(service, "buildHostingEfficiency", 3, 1.5));
        Assertions.assertEquals("Bad", ReflectionTestUtils.invokeMethod(service, "buildHostingEfficiency", 2, 2.6));
        Assertions.assertEquals("Bad", ReflectionTestUtils.invokeMethod(service, "buildHostingEfficiency", 4, 2.5));
    }
}
