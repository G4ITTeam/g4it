/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiinventory.repository;

import com.soprasteria.g4it.backend.apiinventory.modeldb.PhysicalEquipment;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/physicalEquipment.sql")
@ActiveProfiles({"test"})
class PhysicalEquipmentRepositoryTest {
    @Autowired
    private PhysicalEquipmentRepository physicalEquipmentRepository;
    @Autowired
    private InventoryRepository inventoryRepository;

    @Test
    void findByUniqueConstraintShouldReturnEntity() {
        // Given
        final long inventoryId = 301;
        final String uniqueName = "Serveur 9";

        assertThat(physicalEquipmentRepository.findByInventoryIdAndNomEquipementPhysique(inventoryId, uniqueName)).isPresent();
    }

    @Test
    void findByUniqueConstraintShouldNotReturnEntityIfNotMatch() {
        // Given
        final long inventoryId = 301;
        final String unknownName = "Serveur 99";

        assertThat(physicalEquipmentRepository.findByInventoryIdAndNomEquipementPhysique(inventoryId, unknownName)).isEmpty();
    }

    @Test
    void findBySessionDateShouldReturnOnlyPhysicalEquipmentOfSessionDate() throws Exception {
        // Given
        final Date dateSession = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2023-04-02 02:00:00");

        // when we look up by session date
        final Page<PhysicalEquipment> results = physicalEquipmentRepository.findBySessionDate(dateSession, Pageable.ofSize(10));

        assertThat(results).hasSize(1)
                .anyMatch(equipment -> "Serveur 7".equals(equipment.getNomEquipementPhysique()))
                .noneMatch(equipment -> "Serveur 9".equals(equipment.getNomEquipementPhysique()));
    }

    @Test
    void findByInventoryIdShouldReturnOnlyPhysicalEquipmentOfInventoryId() {
        // Given
        final long inventoryId = 301;

        // when we lookup by InventoryId;
        final Page<PhysicalEquipment> results = physicalEquipmentRepository.findByInventoryId(inventoryId, Pageable.ofSize(10));

        // then there should be only one physical equipment
        assertThat(results).hasSize(4)
                .anyMatch(physicalEquipmentDb -> "Serveur 10".equals(physicalEquipmentDb.getNomEquipementPhysique()));
    }

    @Test
    void countByInventoryIdShouldSumUpQuantiteOfPhysicalEquipmentOfInventoryId() {
        // Given
        final long inventoryId = 301;

        assertThat(physicalEquipmentRepository.countByInventoryId(inventoryId)).isEqualTo(11);
    }

}
