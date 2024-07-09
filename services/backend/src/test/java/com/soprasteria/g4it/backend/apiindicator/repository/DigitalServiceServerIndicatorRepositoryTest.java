/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceServerIndicatorView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/sql/digital-service-server-indicator.sql")
class DigitalServiceServerIndicatorRepositoryTest {

    @Autowired
    DigitalServiceServerIndicatorRepository digitalServiceServerIndicatorRepository;

    @Test
    void givenValidParam_shouldGedDigitalServiceServerIndicators() {
        //GIVEN
        final String uid = "uid";

        final List<DigitalServiceServerIndicatorView> indicatorViewList = digitalServiceServerIndicatorRepository.findDigitalServiceServerIndicators(uid);

        assertThat(indicatorViewList).hasSize(10);
    }

    @Test
    void givenUnknownUid_shouldGedNoServerIndicators() {
        //GIVEN
        final String uid = "Unknown uid";

        final List<DigitalServiceServerIndicatorView> indicatorViewList = digitalServiceServerIndicatorRepository.findDigitalServiceServerIndicators(uid);

        assertThat(indicatorViewList).isEmpty();
    }
}
