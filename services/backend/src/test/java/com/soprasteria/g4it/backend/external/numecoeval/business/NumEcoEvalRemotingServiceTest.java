/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.external.numecoeval.business;

import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.RapportImportRest;
import com.soprasteria.g4it.backend.client.gen.connector.apiexposition.dto.StatutCalculRest;
import com.soprasteria.g4it.backend.exception.NumEcoEvalConnectorRuntimeException;
import com.soprasteria.g4it.backend.external.numecoeval.client.CalculationClient;
import com.soprasteria.g4it.backend.external.numecoeval.mapper.NumEcoEvalCalculationReportMapper;
import com.soprasteria.g4it.backend.external.numecoeval.mapper.NumEcoEvalInputReportMapper;
import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalInputReport;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalCalculationReportRepository;
import com.soprasteria.g4it.backend.external.numecoeval.repository.NumEcoEvalInputReportRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NumEcoEvalRemotingServiceTest {

    @Mock
    private CalculationClient calculationClient;
    @Mock
    private NumEcoEvalInputReportMapper numEcoEvalInputReportMapper;
    @Mock
    private NumEcoEvalCalculationReportMapper numEcoEvalCalculationReportMapper;
    @Mock
    private NumEcoEvalInputReportRepository numEcoEvalInputReportRepository;
    @Mock
    private NumEcoEvalCalculationReportRepository numEcoEvalCalculationReportRepository;
    private static final String testFolderFile = "external/numecoeval/file";
    @InjectMocks
    private NumEcoEvalRemotingService numEcoEvalRemotingService;

    @Test
    void givenReferenceInput_whenCallNumEcoEvalInputData_thenReturnReports() throws Exception {
        // Given
        final Resource datacenter = new ClassPathResource(testFolderFile + "/datacenter.csv");
        final Resource physicalEquipment = new ClassPathResource(testFolderFile + "/physical_equipment.csv");
        final Resource virtualEquipment = new ClassPathResource(testFolderFile + "/virtual_equipment.csv");
        final Resource application = new ClassPathResource(testFolderFile + "/application.csv");
        final String organization = "test";
        final String dateLot = "2023-01-01";
        final String batchName = "batchName";


        // Mock Repository
        var rapportImportRest = List.of(new RapportImportRest());
        when(calculationClient.importCSV(any(), any(), any(), any(), any(), any(), any())).thenReturn(rapportImportRest);
        final List<NumEcoEvalInputReport> entities = List.of(NumEcoEvalInputReport.builder().build());
        when(numEcoEvalInputReportMapper.toEntities(rapportImportRest, batchName)).thenReturn(entities);
        when(numEcoEvalInputReportRepository.saveAll(entities)).thenReturn(entities);

        // Call
        assertDoesNotThrow(() -> numEcoEvalRemotingService.callInputDataExposition(
                datacenter, physicalEquipment, virtualEquipment, application, organization, dateLot, batchName));

        verify(numEcoEvalInputReportMapper, times(1)).toEntities(rapportImportRest, batchName);
        verify(numEcoEvalInputReportRepository, times(1)).saveAll(entities);
    }


    @Test
    void givenResource_whenFilesDoNotExist_thenServiceThrow() {
        // Given
        final Resource datacenter = new ClassPathResource(testFolderFile + "/unknown.csv");
        final Resource physicalEquipment = new ClassPathResource(testFolderFile + "/unknown.csv");
        final Resource virtualEquipment = new ClassPathResource(testFolderFile + "/unknown.csv");
        final Resource application = new ClassPathResource(testFolderFile + "/unknown.csv");
        final String organization = "test";
        final String dateLot = "2023-01-01";
        final String batchName = "batchName";

        assertThatThrownBy(() -> numEcoEvalRemotingService.callInputDataExposition(datacenter, physicalEquipment, virtualEquipment, application, organization, dateLot, batchName))
                .isInstanceOf(NumEcoEvalConnectorRuntimeException.class)
                .hasMessageContaining("No file generated");

        assertThatThrownBy(() -> numEcoEvalRemotingService.callInputDataExposition(null, null, null, application, organization, dateLot, batchName))
                .isInstanceOf(NumEcoEvalConnectorRuntimeException.class)
                .hasMessageContaining("No file generated");
    }

    @Test
    void shouldGetCalculationProgress() {
        StatutCalculRest statutCalculRest = new StatutCalculRest();
        statutCalculRest.setEtat("50%");
        Mockito.when(calculationClient.getCalculationsStatus(any(), anyString())).thenReturn(statutCalculRest);

        String batchName = "db501e7a-871f-4a61-8672-0947bdc82a3e";
        String organization = "DEMO-1";
        // Call
        var progressPercentage = numEcoEvalRemotingService.getCalculationsProgress(batchName, organization);

        assertThat(progressPercentage).isEqualTo("50%");
    }

}
