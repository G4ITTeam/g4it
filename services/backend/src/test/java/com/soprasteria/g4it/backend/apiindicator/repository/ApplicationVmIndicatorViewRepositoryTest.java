/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationVmIndicatorView;
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
class ApplicationVmIndicatorViewRepositoryTest {

    @Autowired
    private ApplicationVmIndicatorViewRepository applicationVmIndicatorViewRepository;

    private final static String criteria = "Climate change";
    private final static String batchName = "44ad614e-94ad-46fb-b541-5053d8a45ee6";
    private final static Long inventoryId = 501L;

    @Test
    void givenValidParam_shouldGetEquipmentIndicators() {
        final String organizationName = "SSG";
        final String applicationName = "application-1";
        final List<ApplicationVmIndicatorView> indicators = applicationVmIndicatorViewRepository.findIndicators(
                organizationName, batchName, inventoryId,
                applicationName, criteria);

        assertThat(indicators).hasSize(8)
                .extracting(ApplicationVmIndicatorView::getEquipmentType,
                        ApplicationVmIndicatorView::getCluster,
                        ApplicationVmIndicatorView::getImpact,
                        ApplicationVmIndicatorView::getSip)
                .contains(Tuple.tuple("Personal Computer", "PY1LNX02", 308d, 0.3618208516886931));
    }

    @Test
    void givenUnknownParam_shouldGedEquipmentIndicators() {
        final String organizationName = "Unknown";
        final String applicationName = "Unknown-1";

        final List<ApplicationVmIndicatorView> indicators = applicationVmIndicatorViewRepository.findIndicators(
                organizationName, batchName, inventoryId,
                applicationName, criteria);

        assertThat(indicators).isEmpty();
    }

}
