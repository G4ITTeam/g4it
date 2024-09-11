/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.AggApplicationIndicator;
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
class AggApplicationIndicatorRepositoryTest {

    @Autowired
    private AggApplicationIndicatorRepository aggApplicationIndicatorRepository;

    @Test
    void givenValidParam_shouldGetEquipmentIndicators() {
        final String batchName = "44ad614e-94ad-46fb-b541-5053d8a45ee6";

        aggApplicationIndicatorRepository.insertIntoAggApplicationIndicators(batchName, 501L);
        final List<AggApplicationIndicator> indicators = aggApplicationIndicatorRepository.findByBatchNameAndInventoryId(batchName, 501L);
        assertThat(indicators).hasSize(64)
                .filteredOn(e -> "application-1".equals(e.getApplicationName())
                        && "FABRICATION".equals(e.getLifeCycle())
                        && "Climate change".equals(e.getCriteria()))
                .extracting(AggApplicationIndicator::getEquipmentType,
                        AggApplicationIndicator::getDomain,
                        AggApplicationIndicator::getSubDomain,
                        AggApplicationIndicator::getImpact,
                        AggApplicationIndicator::getSip)
                .hasSize(2)
                .contains(Tuple.tuple("SSG_Personal Computer", "Domain 1", "Sub domain 1", 616d, 0.7236417033773862),
                        Tuple.tuple("SSG_Personal Computer", "", "", 308d, 0.3618208516886931));
    }

    @Test
    void givenUnknownParam_shouldGedEquipmentIndicators() {
        final String batchName = "44ad614e-94ad-46fb-b541-5053d8a45ee6";

        final List<AggApplicationIndicator> aggApplicationIndicator = aggApplicationIndicatorRepository.findByBatchNameAndInventoryId(batchName, 1L);

        assertThat(aggApplicationIndicator).isEmpty();
    }

}
