/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentLowImpactView;
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
class PhysicalEquipmentLowImpactViewRepositoryTest {

    @Autowired
    private PhysicalEquipmentLowImpactViewRepository physicalEquipmentLowImpactViewRepository;

    @Test
    void givenUnknownParam_shouldGedPhysicalEquipmentLowImpactIndicators() {
        final String subscriber = "SSG";
        final String organization = "bad organization";
        final Long inventoryId = 601L;

        final List<PhysicalEquipmentLowImpactView> indicatorView = physicalEquipmentLowImpactViewRepository
                .findPhysicalEquipmentLowImpactIndicators(subscriber, organization, inventoryId);

        assertThat(indicatorView).isEmpty();
    }

    @Test
    void givenValidParam_shouldGedPhysicalEquipmentLowImpactIndicators() {
        final String subscriber = "SSG";
        final String organization = "G4IT";
        final Long inventoryId = 601L;

        final List<PhysicalEquipmentLowImpactView> indicatorView = physicalEquipmentLowImpactViewRepository.findPhysicalEquipmentLowImpactIndicators(subscriber, organization, inventoryId);

        assertThat(indicatorView).hasSize(2);
        assertThat(indicatorView.stream().allMatch(PhysicalEquipmentLowImpactView::getLowImpact)).isEqualTo(false);
    }

}
