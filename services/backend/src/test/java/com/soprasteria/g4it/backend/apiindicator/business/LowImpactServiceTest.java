/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.business;

import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class LowImpactServiceTest {

    @InjectMocks
    LowImpactService lowImpactService;

    @Mock
    NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    @Test
    void shouldGetQuartileIndexForInventory() {

        Map<Pair<String, String>, Integer> quartileMap = Map.of(
                Pair.of("France", "C1"), 1,
                Pair.of("France", "C2"), 1,
                Pair.of("Germany", "C1"), 4,
                Pair.of("Germany", "C2"), 4
        );

        ReflectionTestUtils.setField(lowImpactService, "criterias", Set.of("C1", "C2"));
        Mockito.when(numEcoEvalReferentialRemotingService.getCountryList()).thenReturn(List.of("France", "Germany"));
        Mockito.when(numEcoEvalReferentialRemotingService.getElectricityMixQuartiles()).thenReturn(quartileMap);

        assertThat(lowImpactService.isLowImpact("France")).isTrue();
        assertThat(lowImpactService.isLowImpact("Germany")).isFalse();
    }
}
