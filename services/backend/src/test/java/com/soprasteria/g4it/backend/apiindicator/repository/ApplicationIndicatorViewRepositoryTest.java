/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationIndicatorView;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/application-indicator.sql")
@ActiveProfiles({"test"})
class ApplicationIndicatorViewRepositoryTest {

    @Autowired
    private ApplicationIndicatorViewRepository applicationIndicatorViewRepository;

    @Test
    void givenValidParam_shouldGetEquipmentIndicators() {
        final String organizationName = "SSG";
        final String batchName = "44ad614e-94ad-46fb-b541-5053d8a45ee6";

        final List<ApplicationIndicatorView> indicators = applicationIndicatorViewRepository.findIndicators(organizationName, batchName, 501L);

        assertThat(indicators).hasSize(64)
                .filteredOn(e -> "application-1".equals(e.getApplicationName())
                        && "FABRICATION".equals(e.getLifeCycle())
                        && "Climate change".equals(e.getCriteria()))
                .extracting(ApplicationIndicatorView::getEquipmentType,
                        ApplicationIndicatorView::getDomain,
                        ApplicationIndicatorView::getSubDomain,
                        ApplicationIndicatorView::getImpact,
                        ApplicationIndicatorView::getSip)
                .hasSize(2)
                .contains(Tuple.tuple("Personal Computer", "Domain 1", "Sub domain 1", 616d, 0.7236417033773862),
                        Tuple.tuple("Personal Computer", "", "", 308d, 0.3618208516886931));
    }

    @Test
    void givenUnknownParam_shouldGedEquipmentIndicators() {
        final String organizationName = "Unknown";
        final String batchName = "44ad614e-94ad-46fb-b541-5053d8a45ee6";

        final List<ApplicationIndicatorView> equipmentIndicatorView = applicationIndicatorViewRepository.findIndicators(organizationName, batchName, 1L);

        assertThat(equipmentIndicatorView).isEmpty();
    }

}
