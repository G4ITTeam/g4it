/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentIndicatorView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/indicatorview.sql")
@ActiveProfiles({"test"})
class EquipmentIndicatorViewRepositoryTest {

    @Autowired
    private EquipmentIndicatorViewRepository equipmentIndicatorViewRepository;

    @Test
    void givenValidParam_shouldGedEquipmentIndicators() {
        final String batchName = "june 2023";

        final List<EquipmentIndicatorView> equipmentIndicatorView = equipmentIndicatorViewRepository.findIndicators(batchName);

        assertThat(equipmentIndicatorView).hasSize(288);
    }

}
