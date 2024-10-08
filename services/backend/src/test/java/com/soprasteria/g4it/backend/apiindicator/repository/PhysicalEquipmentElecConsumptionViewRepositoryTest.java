/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentElecConsumptionView;
import com.soprasteria.g4it.backend.apiindicator.utils.Constants;
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
public class PhysicalEquipmentElecConsumptionViewRepositoryTest {

    @Autowired
    private PhysicalEquipmentElecConsumptionViewRepository physicalEquipmentElecConsumptionViewRepository;

    @Test
    void givenValidParam_shouldGedPhysicalEquipmentElecConsumptionIndicators() {
        final String batchName = "june 2023";

        final List<PhysicalEquipmentElecConsumptionView> indicatorView = physicalEquipmentElecConsumptionViewRepository.findPhysicalEquipmentElecConsumptionIndicators(batchName, Constants.CRITERIA_NUMBER);

        assertThat(indicatorView).hasSize(16);
    }

}
