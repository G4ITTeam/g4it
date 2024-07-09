/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.business;


import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationDomainsFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentFiltersBO;
import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationFilters;
import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentFilters;
import com.soprasteria.g4it.backend.apiindicator.repository.ApplicationFiltersRepository;
import com.soprasteria.g4it.backend.apiindicator.repository.EquipmentFiltersRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
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
        final Long inventoryId = 1L;
        final String batchName = "batchName";

        final EquipmentFilters filter = EquipmentFilters.builder()
                .country("France")
                .entity("SSG")
                .type("Serveur")
                .status("InUse").build();
        List<EquipmentFilters> equipmentFiltersList = new ArrayList<>();
        equipmentFiltersList.add(filter);

        when(this.equipmentFiltersRepository.getFiltersByInventoryId(inventoryId, batchName)).thenReturn(equipmentFiltersList);

        this.filterService.getEquipmentFilters(subscriber, organization, inventoryId, batchName);

        verify(this.equipmentFiltersRepository, times(1)).getFiltersByInventoryId(inventoryId, batchName);
    }

    @Test
    void getEquipmentFilters_whenDataNotExists() {
        final Long inventoryId = 1L;
        final String batchName = "batchName";

        when(equipmentFiltersRepository.getFiltersByInventoryId(inventoryId, batchName)).thenReturn(new ArrayList<>());

        final EquipmentFiltersBO filters = filterService.getEquipmentFilters(subscriber, organization, inventoryId, batchName);

        Assertions.assertThat(filters.getCountries()).isEmpty();
        Assertions.assertThat(filters.getEntities()).isEmpty();
        Assertions.assertThat(filters.getStatus()).isEmpty();
        Assertions.assertThat(filters.getEquipments()).isEmpty();

        verify(equipmentFiltersRepository, times(1)).getFiltersByInventoryId(inventoryId, batchName);
    }

    @Test
    void shouldReturnApplicationFilters() {
        final Long inventoryId = 1L;
        final String batchName = "batchName";

        final List<ApplicationFilters> filters = List.of(
                ApplicationFilters.builder()
                        .id(1)
                        .type("type1")
                        .domain("domain1")
                        .subDomain("domain1subdomain1")
                        .lifeCycle("lifeCycle1")
                        .environment("env1")
                        .build(),
                ApplicationFilters.builder()
                        .id(2)
                        .type("type1")
                        .domain("domain1")
                        .subDomain("domain1subdomain2")
                        .lifeCycle("lifeCycle2")
                        .environment("env1")
                        .build(),
                ApplicationFilters.builder()
                        .id(3)
                        .type("type2")
                        .domain("domain2")
                        .subDomain("domain2subdomain1")
                        .lifeCycle("lifeCycle3")
                        .environment("env2")
                        .build(),
                ApplicationFilters.builder()
                        .id(4)
                        .type("type3")
                        .domain("domain2")
                        .subDomain("domain2subdomain2")
                        .lifeCycle("lifeCycle4")
                        .environment("env3")
                        .build()
        );

        when(this.applicationFiltersRepository.getFiltersByBatchName(inventoryId, batchName)).thenReturn(filters);

        final ApplicationFiltersBO actualFilters = this.filterService.getApplicationFilters(subscriber, organization.getId(), inventoryId, batchName, null, null, null);
        Assertions.assertThat(actualFilters.getLifeCycles()).hasSize(4).contains("lifeCycle1", "lifeCycle2", "lifeCycle3", "lifeCycle4");
        Assertions.assertThat(actualFilters.getEnvironments()).hasSize(3).contains("env1", "env2", "env3");
        Assertions.assertThat(actualFilters.getTypes()).hasSize(3).contains("type1", "type2", "type3");
        Assertions.assertThat(actualFilters.getDomains()).hasSize(2).extracting(ApplicationDomainsFiltersBO::getName, ApplicationDomainsFiltersBO::getSubDomains)
                .contains(Tuple.tuple("domain1", List.of("domain1subdomain1", "domain1subdomain2")),
                        Tuple.tuple("domain2", List.of("domain2subdomain1", "domain2subdomain2")));

        verify(this.applicationFiltersRepository, times(1)).getFiltersByBatchName(inventoryId, batchName);
    }

    @Test
    void getApplicationFilters_whenDataNotExists() {
        final Long inventoryId = 1L;
        final String batchName = "batchName";

        when(applicationFiltersRepository.getFiltersByBatchName(inventoryId, batchName)).thenReturn(new ArrayList<>());

        final ApplicationFiltersBO filters = filterService.getApplicationFilters(subscriber, organization.getId(), inventoryId, batchName, null, null, null);

        Assertions.assertThat(filters.getDomains()).isEmpty();
        Assertions.assertThat(filters.getEnvironments()).isEmpty();
        Assertions.assertThat(filters.getTypes()).isEmpty();
        Assertions.assertThat(filters.getLifeCycles()).isEmpty();

        verify(applicationFiltersRepository, times(1)).getFiltersByBatchName(inventoryId, batchName);
    }
}
