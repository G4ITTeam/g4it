/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceIndicatorView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/sql/digital-service-indicator.sql")
class DigitalServiceIndicatorRepositoryTest {

    @Autowired
    private DigitalServiceIndicatorRepository digitalServiceIndicatorRepository;

    @Test
    void givenValidParam_shouldGedDigitalServiceIndicators() {
        //GIVEN
        final String uid = "uid";

        final List<DigitalServiceIndicatorView> indicatorView = digitalServiceIndicatorRepository.findDigitalServiceIndicators(uid);

        assertThat(indicatorView).hasSize(5);
    }

    @Test
    void givenUnknownUid_shouldGedIndicators() {
        final String uid = "Bad uid";

        final List<DigitalServiceIndicatorView> indicatorView = digitalServiceIndicatorRepository.findDigitalServiceIndicators(uid);

        assertThat(indicatorView).isEmpty();
    }

}
