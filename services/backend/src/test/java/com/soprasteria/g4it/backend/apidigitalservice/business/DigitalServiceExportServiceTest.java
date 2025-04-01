/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apidigitalservice.business;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalService;
import com.soprasteria.g4it.backend.apidigitalservice.repository.DigitalServiceRepository;
import com.soprasteria.g4it.backend.apifiles.business.FileSystemService;
import com.soprasteria.g4it.backend.apiindicator.business.DigitalServiceExportService;
import com.soprasteria.g4it.backend.apiindicator.repository.numecoeval.PhysicalEquipmentIndicatorRepository;
import com.soprasteria.g4it.backend.apiinout.repository.InVirtualEquipmentRepository;
import com.soprasteria.g4it.backend.common.criteria.CriteriaService;
import com.soprasteria.g4it.backend.common.filesystem.business.local.LocalFileService;
import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.task.repository.TaskRepository;
import com.soprasteria.g4it.backend.exception.G4itRestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class DigitalServiceExportServiceTest {

    final static Long ORGANIZATION_ID = 1L;
    final static String SUBSCRIBER = "SOPRA-STERIA-TEST";
    @Mock
    private DigitalServiceRepository digitalServiceRepository;
    @Mock
    private PhysicalEquipmentIndicatorRepository physicalEquipmentIndicatorRepository;

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
    void whenGetNotExistDigitalService_thenThrow() {
        final String digitalServiceUid = "80651485-3f8b-49dd-a7be-753e4fe1fd36";
        Mockito.lenient().when(digitalServiceRepository.findById(digitalServiceUid)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> exportService.createFiles(digitalServiceUid, SUBSCRIBER, ORGANIZATION_ID))
                .isInstanceOf(G4itRestException.class);
    }

    DigitalService getDigitalService() {
        DigitalService digitalService = new DigitalService();
        digitalService.setName("Digital Service 1");
        digitalService.setUid("9825726b-2c74-4f1e-800d-15de592d6d3f");
        digitalService.setCreationDate(LocalDateTime.now());
        return digitalService;
    }
}
