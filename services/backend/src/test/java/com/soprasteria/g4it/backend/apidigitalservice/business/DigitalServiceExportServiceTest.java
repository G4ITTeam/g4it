/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.*;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DatacenterDigitalServiceRepository;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceExportService;
import com.soprasteria.g4it.backend.apiindicator.repository.numecoeval.PhysicalEquipmentIndicatorRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaByType;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.common.task.modeldb.Task;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DigitalServiceExportServiceTest {

    final static Long ORGANIZATION_ID = 1L;
    final static String SUBSCRIBER = "SOPRA-STERIA-TEST";
    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private PhysicalEquipmentIndicatorRepository physicalEquipmentIndicatorRepository;
    @Mock
    private DatacenterDigitalServiceRepository datacenterDigitalServiceRepository;
    @Mock
    private InVirtualEquipmentRepository inVirtualEquipmentRepository;
    @InjectMocks
    private DigitalServiceExportService exportService;
    @Mock
    private LocalFileService localFileService;
    @Mock
    private FileSystemService fileSystemService;
    @Mock
    private FileMapperInfo fileInfo;
    @Mock
    private CriteriaService criteriaService;
    @Mock
    private TaskRepository taskRepository;

    @Test
    void shouldCreateFile() throws Exception {
        Optional<DigitalService> digitalService = Optional.of(getDigitalService());
        final String digitalServiceUid = digitalService.get().getUid();
        final List<String> criteriaList = List.of("ionising-radiation", "climate-change");
        digitalService.get().setCriteria(criteriaList);

        Mockito.lenient().when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(digitalService);
        Mockito.lenient().when(datacenterDigitalServiceRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(List.of(DatacenterDigitalService.builder().name("datacenter1").pue(BigDecimal.valueOf(1.75)).location("France").digitalService(getDigitalService()).build()));
        Mockito.lenient().when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(digitalService);
        Mockito.lenient().when(physicalEquipmentIndicatorRepository.findByBatchName(any(), any())).thenReturn(Page.empty());
        Mockito.lenient().when(inVirtualEquipmentRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(List.of());
        Mockito.lenient().when(fileInfo.getMapping(FileType.PHYSICAL_EQUIPMENT_INDICATOR_DIGITAL_SERVICE)).thenReturn(List.of(Header.builder().build()));
        Mockito.lenient().when(fileInfo.getMapping(FileType.DATACENTER_DIGITAL_SERVICE)).thenReturn(List.of(Header.builder().build()));
        Mockito.lenient().when(fileInfo.getMapping(FileType.NETWORK)).thenReturn(List.of(Header.builder().build()));
        Mockito.lenient().when(fileInfo.getMapping(FileType.SERVER)).thenReturn(List.of(Header.builder().build()));
        Mockito.lenient().when(fileInfo.getMapping(FileType.TERMINAL)).thenReturn(List.of(Header.builder().build()));
        Mockito.lenient().when(fileInfo.getMapping(FileType.CLOUD_INSTANCE)).thenReturn(List.of(Header.builder().build()));
        Mockito.doCallRealMethod().when(localFileService).writeFile(any(), any());
        Mockito.when(localFileService.createZipFile(any(), any())).thenCallRealMethod();
        when(criteriaService.getSelectedCriteriaForDigitalService(SUBSCRIBER, ORGANIZATION_ID, criteriaList)).thenReturn(new CriteriaByType(criteriaList, null, null, null, null, null));

        Mockito.lenient().when(taskRepository.findByDigitalServiceUid(digitalServiceUid)).thenReturn(Optional.of(Task.builder().id(1L).digitalServiceUid(digitalServiceUid).build()));
        ReflectionTestUtils.setField(exportService, "localWorkingFolder", "target");

        final InputStream result = exportService.createFiles(digitalServiceUid, SUBSCRIBER, ORGANIZATION_ID);

        verify(digitalServiceRepository, times(1)).findById(digitalServiceUid);
        verify(physicalEquipmentIndicatorRepository, times(1)).findByBatchName(eq(digitalServiceUid), any());
        verify(fileInfo, times(1)).getMapping(FileType.TERMINAL);
        assertThat(result).isNotEmpty();
    }

    @Test
    void whenGetNotExistDigitalService_thenThrow() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        Mockito.lenient().when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> exportService.createFiles(digitalServiceUid, SUBSCRIBER, ORGANIZATION_ID))
                .isInstanceOf(G4itRestException.class);
    }

    DigitalService getDigitalService() {
        DigitalService digitalService = new DigitalService();
        digitalService.setDatacenterDigitalServices(List.of(DatacenterDigitalService.builder().name("Default DC").pue(BigDecimal.valueOf(1.5)).location("France").build()));
        digitalService.setName("Digital Service 1");
        digitalService.setNetworks(List.of(Network.builder().networkType(NetworkTypeRef.builder().type("Fixed").build()).yearlyQuantityOfGbExchanged(21.0).build()));
        digitalService.setUid("9825726b-2c74-4f1e-800d-15de592d6d3f");
        digitalService.setCreationDate(LocalDateTime.now());
        digitalService.setTerminals(List.of(Terminal.builder().deviceType(DeviceTypeRef.builder().id(12L).description("Landline").build()).country("India").numberOfUsers(2).lifespan(9.0).yearlyUsageTimePerUser(5.0).build()));
        digitalService.setServers(List.of(Server.builder().name("Server A").type("Compute").mutualizationType("DEDICATED").serverHost(ServerHostRef.builder().description("server").externalReferentialDescription("extref").build()).quantity(2).lifespan(2.0).annualElectricityConsumption(3).build()));
        digitalService.setIsNewArch(false);
        return digitalService;
    }
}
