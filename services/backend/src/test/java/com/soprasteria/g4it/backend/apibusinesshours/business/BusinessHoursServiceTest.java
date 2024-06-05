/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apibusinesshours.business;

import com.soprasteria.g4it.backend.apibusinesshours.mapper.BusinessHoursMapperImpl;
import com.soprasteria.g4it.backend.apibusinesshours.modeldb.BusinessHours;
import com.soprasteria.g4it.backend.apibusinesshours.repository.BusinessHoursRepository;
import com.soprasteria.g4it.backend.server.gen.api.dto.BusinessHoursRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BusinessHoursServiceTest {
    @InjectMocks
    private BusinessHoursService service;

    @Mock
    private BusinessHoursRepository repository;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(service, "mapper", new BusinessHoursMapperImpl());
    }

    @Test
    void canRetrieveBusinessHours() {
        final BusinessHoursRest businessHours1 = BusinessHoursRest.builder().build();
        final BusinessHoursRest businessHours2 = BusinessHoursRest.builder().build();
        final List<BusinessHoursRest> expectedList = List.of(businessHours1, businessHours2);

        final BusinessHours businessEntity1 = BusinessHours.builder().id(1L).day("Monday").startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build();
        final BusinessHours businessEntity2 = BusinessHours.builder().id(2L).day("Tuesday").startTime(LocalDateTime.now()).endTime(LocalDateTime.now()).build();
        final List<BusinessHours> businessEntitiesList = List.of(businessEntity1, businessEntity2);

        when(repository.findAll(Sort.by(new Sort.Order(Sort.Direction.ASC, "id").ignoreCase()))).thenReturn(businessEntitiesList);
        final List<BusinessHoursRest> result = service.getBusinessHours();
        assertThat(result).hasSameSizeAs(expectedList);
        verify(repository, times(1)).findAll(Sort.by(new Sort.Order(Sort.Direction.ASC, "id").ignoreCase()));
    }

}
