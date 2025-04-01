// TODO -- update the test case
///*
// * G4IT
// * Copyright 2023 Sopra Steria
// *
// * This product includes software developed by
// * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
// */
//package com.soprasteria.g4it.backend.apiinventory.repository;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
//import org.springframework.dao.DataIntegrityViolationException;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.jdbc.Sql;
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.util.Date;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//
//@DataJpaTest
//@Sql("/sql/application.sql")
//@ActiveProfiles({"test"})
//class ApplicationRepositoryTest {
//    @Autowired
//    private ApplicationRepository applicationRepository;
//    @Autowired
//    private InventoryRepository inventoryRepository;
//
//    @Test
//    void shouldNotCreateApplicationWithoutInventory() {
//        final Application application = Application.builder()
//                .creationDate(LocalDateTime.of(2023, 3, 12, 9, 0, 0))
//                .nomApplication("application_test_shouldNotCreateApplicationWithoutInventory")
//                .typeEnvironnement("PROD")
//                .nomEquipementVirtuel("vm_mut")
//                .nomEquipementPhysique("Serveur_256")
//                .valid(true)
//                .sessionDate(new Date())
//                .build();
//
//        assertThatThrownBy(() -> applicationRepository.save(application))
//                .isInstanceOf(DataIntegrityViolationException.class)
//                .hasMessageContaining("APPLICATION_INVENTORY_ID_FK");
//    }
//
//    @Test
//    void findInventoryIdShouldReturnOnlyApplicationOfInventoryId() {
//        // Given
//        final long inventoryId = 502;
//
//        // when we look up by organisation and InventoryId then there should be only one virtual equipment
//        assertThat(applicationRepository.findByInventoryId(inventoryId, Pageable.ofSize(50)))
//                .hasSize(6)
//                .anyMatch(application -> "Application 20".equals(application.getNomApplication()));
//    }
//
//    @Test
//    void findByUniqueConstraint_shouldReturnOnlyOneResult() throws Exception {
//        // Given
//        final long inventoryId = 501;
//
//        // when we look up by unique constraint
//        // Then it should fetch only one result
//        assertThat(
//                applicationRepository
//                        .findByInventoryIdAndNomApplicationAndTypeEnvironnementAndNomEquipementVirtuel(
//                                inventoryId,
//                                "Application 9",
//                                "PROD",
//                                "VM 06"))
//                .isPresent();
//    }
//
//    @Test
//    void findBySessionDate_shouldReturnOnlyOneResult() throws Exception {
//        // Given
//        final Date dateSession = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2023-04-02 02:00:00");
//
//        // when we look up by unique constraint
//        final Page<Application> result = applicationRepository
//                .findBySessionDate(dateSession, Pageable.ofSize(50));
//
//        // Then it should fetch only one result
//        assertThat(result).hasSize(6);
//    }
//
//    @Test
//    void countDistinctApplicationNameByInventoryId_shouldReturnDistinctCount() {
//        // Given
//        final Long inventoryId = 502L;
//
//        // Tested method
//        final Long count = applicationRepository.countDistinctNomApplicationByInventoryId(inventoryId);
//
//        // Validation
//        assertThat(count).isEqualTo(6);
//    }
//
//}
