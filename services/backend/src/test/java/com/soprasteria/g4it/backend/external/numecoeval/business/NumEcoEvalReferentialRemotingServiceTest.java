/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.MixElectriqueDTO;
import com.soprasteria.g4it.backend.client.gen.connector.apireferentiel.dto.TypeEquipementDTO;
import com.soprasteria.g4it.backend.external.numecoeval.client.ReferentialClient;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NumEcoEvalReferentialRemotingServiceTest {

    @InjectMocks
    NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;

    @Mock
    ReferentialClient referentialClient;

    private static final String TESTFOLDERREF = "external/numecoeval/referential";

    @Test
    void shouldGetCountry() {
        var mixFR = new MixElectriqueDTO();
        mixFR.setPays("France");
        var mixEN = new MixElectriqueDTO();
        mixEN.setPays("England");

        Mockito.when(referentialClient.getMixElec()).thenReturn(List.of(mixFR, mixEN));

        // Call
        var country = numEcoEvalReferentialRemotingService.getCountryList();

        assertThat(country).hasSize(2).contains("France", "England");
    }

    @Test
    void shouldGetCountry_empty() {
        Mockito.when(referentialClient.getMixElec()).thenReturn(List.of());

        // Call
        var country = numEcoEvalReferentialRemotingService.getCountryList();

        assertThat(country).isEmpty();
    }


    @Test
    void shouldGetQuartileIndex() throws IOException {

        final File mixElecCsvFile = new ClassPathResource(TESTFOLDERREF + "/mixElec.json").getFile();
        List<MixElectriqueDTO> mockMixElec = Arrays.asList(new ObjectMapper().readValue(Files.readString(mixElecCsvFile.toPath()), MixElectriqueDTO[].class));

        Mockito.when(referentialClient.getMixElec()).thenReturn(mockMixElec);

        assertThat(numEcoEvalReferentialRemotingService.getElectricityMixQuartiles()).containsEntry(Pair.of("Albania", "C1"), 1);
        assertThat(numEcoEvalReferentialRemotingService.getElectricityMixQuartiles()).containsEntry(Pair.of("Angola", "C2"), 2);
        assertThat(numEcoEvalReferentialRemotingService.getElectricityMixQuartiles()).containsEntry(Pair.of("Armenia", "C4"), 4);
    }


    @Test
    void shouldGetEquipmentType() {
        var eq1 = new TypeEquipementDTO();
        eq1.setType("Terminal");
        var eq2 = new TypeEquipementDTO();
        eq2.setType("Network");

        Mockito.when(referentialClient.getEquipementTypes()).thenReturn(List.of(eq1, eq2));

        // Call
        var equipmentTypeList = numEcoEvalReferentialRemotingService.getEquipmentTypeList();

        assertThat(equipmentTypeList).containsExactly("Terminal", "Network");
    }


}
