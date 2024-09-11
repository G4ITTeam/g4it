/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.Filters;
import com.soprasteria.g4it.backend.apiindicator.repository.ApplicationFiltersRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.EquipmentFiltersRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterServiceTest {

    @Mock
    private EquipmentFiltersRepository equipmentFiltersRepository;

    @Mock
    private ApplicationFiltersRepository applicationFiltersRepository;

    @Mock
    private OrganizationService organizationService;

    @InjectMocks
    private FilterService filterService;

    private static final String subscriber = TestUtils.createSubscriber().getName();
    private static final Organization organization = TestUtils.createOrganization();

    @BeforeEach
    void init() {
        lenient().when(organizationService.getOrganizationById(any())).thenReturn(organization);
    }

    @Test
    void shouldReturnEquipmentFilters() {
        final String batchName = "batchName";

        when(this.equipmentFiltersRepository.getFiltersByBatchName(batchName)).thenReturn(List.of(
                Filters.builder().field("country").values(new String[]{"France", "Germany"}).build(),
                Filters.builder().field("type").values(new String[]{"Server", "SUBSCRIBER_SubType", "KO_SubType"}).build()
        ));

        EquipmentFiltersBO actual = this.filterService.getEquipmentFilters(subscriber, organization, batchName);
        Assertions.assertThat(actual.getCountries()).contains("France", "Germany");
        Assertions.assertThat(actual.getEquipments()).contains("Server", "SubType", "KO_SubType");
    }

    @Test
    void getEquipmentFilters_whenDataNotExists() {
        final String batchName = "batchName";

        when(equipmentFiltersRepository.getFiltersByBatchName(batchName)).thenReturn(List.of());

        final EquipmentFiltersBO filters = filterService.getEquipmentFilters(subscriber, organization, batchName);

        Assertions.assertThat(filters.getCountries()).isEmpty();
        Assertions.assertThat(filters.getEntities()).isEmpty();
        Assertions.assertThat(filters.getStatus()).isEmpty();
        Assertions.assertThat(filters.getEquipments()).isEmpty();
    }

    @Test
    void shouldReturnApplicationFilters() {
        final String batchName = "batchName";


        when(this.applicationFiltersRepository.getFiltersByBatchName(batchName)).thenReturn(List.of(
                Filters.builder().field("environment").values(new String[]{"env1"}).build(),
                Filters.builder().field("life_cycle").values(new String[]{"lifeCycle1"}).build(),
                Filters.builder().field("type").values(new String[]{"type1", "SUBSCRIBER_SubType"}).build(),
                Filters.builder().field("domain").values(new String[]{"domain1||subD1##subD2", "domain2||subD10##subD20"}).build()
        ));

        final ApplicationFiltersBO actual = this.filterService.getApplicationFilters(subscriber, organization.getId(), batchName, null, null, null);

        Assertions.assertThat(actual.getEnvironments()).contains("env1");
        Assertions.assertThat(actual.getLifeCycles()).contains("lifeCycle1");
        Assertions.assertThat(actual.getTypes()).contains("type1", "SubType");
        Assertions.assertThat(actual.getDomains().get(0).getName()).isEqualTo("domain1");
        Assertions.assertThat(actual.getDomains().get(1).getName()).isEqualTo("domain2");
        Assertions.assertThat(actual.getDomains().get(0).getSubDomains()).contains("subD1", "subD2");
        Assertions.assertThat(actual.getDomains().get(1).getSubDomains()).contains("subD10", "subD20");
    }

    @Test
    void getApplicationFilters_whenDataNotExists() {
        final String batchName = "batchName";

        when(applicationFiltersRepository.getFiltersByBatchName(batchName)).thenReturn(new ArrayList<>());

        final ApplicationFiltersBO filters = filterService.getApplicationFilters(subscriber, organization.getId(), batchName, null, null, null);

        Assertions.assertThat(filters.getDomains()).isEmpty();
        Assertions.assertThat(filters.getEnvironments()).isEmpty();
        Assertions.assertThat(filters.getTypes()).isEmpty();
        Assertions.assertThat(filters.getLifeCycles()).isEmpty();
    }
}
