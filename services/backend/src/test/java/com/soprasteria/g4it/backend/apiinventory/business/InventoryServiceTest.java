/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.business;


import com.soprasteria.g4it.backend.TestUtils;
import com.soprasteria.g4it.backend.apiinventory.mapper.InventoryMapperImpl;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryBO;
import com.soprasteria.g4it.backend.apiinventory.model.InventoryEvaluationReportBO;
import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import com.soprasteria.g4it.backend.apiuser.business.OrganizationService;
import com.soprasteria.g4it.backend.apiuser.modeldb.Organization;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryCreateRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.InventoryType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    private static final String SUBSCRIBER = "SUBSCRIBER";
    private static final String ORGANIZATION = "ORGANIZATION";

    @InjectMocks
    private InventoryService inventoryService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private InventoryRepository inventoryRepo;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(inventoryService, "inventoryMapper", new InventoryMapperImpl());
    }

    @Test
    void canRetrieveAllInventories() {
        final Organization linkedOrganization = TestUtils.createOrganization();

        final InventoryBO inventory1 = InventoryBO.builder().build();
        final InventoryBO inventory2 = InventoryBO.builder().build();
        final List<InventoryBO> expectedInventoryList = List.of(inventory1, inventory2);

        final Inventory inventoryEntity1 = Inventory.builder().id(1L).name("03-2023").build();
        final Inventory inventoryEntity2 = Inventory.builder().id(2L).name("04-2023").build();
        final List<Inventory> inventorysEntitiesList = List.of(inventoryEntity1, inventoryEntity2);

        when(organizationService.getOrganizationBySubNameAndName(SUBSCRIBER, ORGANIZATION)).thenReturn(linkedOrganization);
        when(inventoryRepo.findByOrganization(linkedOrganization)).thenReturn(inventorysEntitiesList);

        final List<InventoryBO> result = inventoryService.getInventories(SUBSCRIBER, ORGANIZATION, null);

        assertThat(result).hasSameSizeAs(expectedInventoryList);

        verify(organizationService, times(1)).getOrganizationBySubNameAndName(SUBSCRIBER, ORGANIZATION);
        verify(inventoryRepo, times(1)).findByOrganization(linkedOrganization);

    }

    @Test
    void canRetrieveInventoriesFilteredByInventoryId() {
        final Organization linkedOrganization = TestUtils.createOrganization();
        final Long inventoryId = 2L;

        final InventoryBO inventory1 = InventoryBO.builder().build();
        final List<InventoryBO> expectedInventoryList = List.of(inventory1);

        final Inventory inventoryEntity1 = Inventory.builder().id(1L).name("03-2023").lastUpdateDate(LocalDateTime.now()).build();
        var inventoryOptional = Optional.of(inventoryEntity1);

        when(organizationService.getOrganizationBySubNameAndName(SUBSCRIBER, ORGANIZATION)).thenReturn(linkedOrganization);
        when(this.inventoryRepo.findByOrganizationAndId(linkedOrganization, inventoryId)).thenReturn(inventoryOptional);

        final List<InventoryBO> result = this.inventoryService.getInventories(SUBSCRIBER, ORGANIZATION, inventoryId);

        assertThat(result).hasSameSizeAs(expectedInventoryList);

        verify(organizationService, times(1)).getOrganizationBySubNameAndName(SUBSCRIBER, ORGANIZATION);
        verify(inventoryRepo, times(1)).findByOrganizationAndId(linkedOrganization, inventoryId);
    }

    @Test
    void canRetrieveOneInventory() {

        final Long inventoryId = 2L;

        final Inventory inventory = Inventory.builder()
                .id(1L)
                .build();

        final InventoryBO expected = InventoryBO.builder()
                .id(1L)
                .dataCenterCount(0L)
                .physicalEquipmentCount(0L)
                .virtualEquipmentCount(0L)
                .applicationCount(0L)
                .integrationReports(List.of())
                .evaluationReports(List.of())
                .build();

        when(inventoryRepo.findByOrganizationAndId(any(), eq(inventoryId))).thenReturn(Optional.of(inventory));

        final InventoryBO result = inventoryService.getInventory(SUBSCRIBER, ORGANIZATION, inventoryId);

        assertThat(result).isEqualTo(expected);

        verify(inventoryRepo, times(1)).findByOrganizationAndId(any(), eq(inventoryId));
    }


    @Test
    void shouldCreateAnInventory() {
        final Organization linkedOrganization = TestUtils.createOrganization();
        final String inventoryName = "03-2023";
        final InventoryCreateRest inventoryCreateRest = InventoryCreateRest.builder()
                .name(inventoryName)
                .type(InventoryType.SIMULATION)
                .build();

        final Inventory inventory = Inventory
                .builder()
                .name("03-2023")
                .organization(linkedOrganization).build();

        when(organizationService.getOrganizationBySubNameAndName(SUBSCRIBER, ORGANIZATION)).thenReturn(linkedOrganization);
        when(inventoryRepo.findByOrganizationAndName(linkedOrganization, inventoryName))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(inventory));
        when(inventoryRepo.save(any())).thenReturn(inventory);

        InventoryBO actual = inventoryService.createInventory(SUBSCRIBER, ORGANIZATION, inventoryCreateRest);

        verify(organizationService, times(1)).getOrganizationBySubNameAndName(SUBSCRIBER, ORGANIZATION);
        verify(inventoryRepo, times(1)).findByOrganizationAndName(linkedOrganization, inventoryCreateRest.getName());
        verify(inventoryRepo, times(1)).save(any());

        assertThat(actual.getName()).isEqualTo("03-2023");
    }

    @Test
    void shouldGetLastBatchName_withoutReport() {
        final InventoryBO inventory = InventoryBO.builder().build();

        assertThat(inventoryService.getLastBatchName(inventory)).isEmpty();
    }

    @Test
    void shouldGetLastBatchName_withOnlyFailedJobs() {
        final InventoryBO inventory = InventoryBO.builder()
                .evaluationReports(List.of(
                        InventoryEvaluationReportBO.builder()
                                .batchName("batchName1")
                                .batchStatusCode("FAILED")
                                .createTime(LocalDateTime.of(2023, 12, 18, 10, 0, 1))
                                .endTime(LocalDateTime.of(2023, 12, 18, 10, 5, 1))
                                .progressPercentage("0%")
                                .build(),
                        InventoryEvaluationReportBO.builder()
                                .batchName("batchName2")
                                .createTime(LocalDateTime.of(2023, 12, 19, 10, 0, 1))
                                .endTime(LocalDateTime.of(2023, 12, 19, 10, 5, 1))
                                .batchStatusCode("FAILED")
                                .progressPercentage("0%")
                                .build()
                ))
                .build();

        assertThat(inventoryService.getLastBatchName(inventory)).isEmpty();
    }

    @Test
    void shouldGetLastBatchName_withCompletedJobs() {
        final InventoryBO inventory = InventoryBO.builder()
                .evaluationReports(List.of(
                        InventoryEvaluationReportBO.builder()
                                .batchName("batchName1")
                                .batchStatusCode("FAILED")
                                .createTime(LocalDateTime.of(2023, 12, 18, 10, 0, 1))
                                .endTime(LocalDateTime.of(2023, 12, 18, 10, 5, 1))
                                .progressPercentage("0%")
                                .build(),
                        InventoryEvaluationReportBO.builder()
                                .batchName("batchName2")
                                .createTime(LocalDateTime.of(2023, 12, 18, 11, 0, 1))
                                .endTime(LocalDateTime.of(2023, 12, 18, 11, 5, 1))
                                .batchStatusCode("COMPLETED")
                                .progressPercentage("100%")
                                .build(),
                        InventoryEvaluationReportBO.builder()
                                .batchName("batchName3")
                                .createTime(LocalDateTime.of(2023, 12, 18, 11, 50, 1))
                                .endTime(LocalDateTime.of(2023, 12, 18, 11, 55, 1))
                                .batchStatusCode("COMPLETED")
                                .progressPercentage("100%")
                                .build(),
                        InventoryEvaluationReportBO.builder()
                                .batchName("batchName4")
                                .createTime(LocalDateTime.of(2023, 12, 19, 10, 0, 1))
                                .endTime(LocalDateTime.of(2023, 12, 19, 10, 5, 1))
                                .batchStatusCode("FAILED")
                                .progressPercentage("0%")
                                .build()
                ))
                .build();

        assertThat(inventoryService.getLastBatchName(inventory)).isEqualTo(Optional.of("batchName3"));
    }


}
