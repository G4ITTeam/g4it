/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceNetworkIndicatorView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/sql/digital-service-network-indicator.sql")
class DigitalServiceNetworkIndicatorRepositoryTest {

    @Autowired
    private DigitalServiceNetworkIndicatorRepository digitalServiceNetworkIndicatorRepository;

    @Test
    void givenValidParam_shouldGedDigitalServiceNetworkIndicators() {
        //GIVEN
        final String uid = "uid";

        List<DigitalServiceNetworkIndicatorView> indicatorViewList = digitalServiceNetworkIndicatorRepository.findDigitalServiceNetworkIndicators(uid);

        assertThat(indicatorViewList).hasSize(5);
    }

    void givenUnknownUid_shouldGedNoIndicators() {
        //GIVEN
        final String uid = "Unknown Uid";

        List<DigitalServiceNetworkIndicatorView> indicatorViewList = digitalServiceNetworkIndicatorRepository.findDigitalServiceNetworkIndicators(uid);

        assertThat(indicatorViewList).isEmpty();
    }

}
