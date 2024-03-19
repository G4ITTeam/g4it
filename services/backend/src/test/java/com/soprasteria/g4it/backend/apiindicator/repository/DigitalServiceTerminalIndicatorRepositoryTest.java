/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceTerminalIndicatorView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/sql/digital-service-terminal-indicator.sql")
class DigitalServiceTerminalIndicatorRepositoryTest {

    @Autowired
    DigitalServiceTerminalIndicatorRepository digitalServiceTerminalIndicatorRepository;

    @Test
    void givenValidParam_shouldGedDigitalServiceTerminalIndicators() {
        //GIVEN
        final String organization = "SSG";
        final String uid = "uid";

        final List<DigitalServiceTerminalIndicatorView> indicatorViewList = digitalServiceTerminalIndicatorRepository.findDigitalServiceTerminalIndicators(organization, uid);

        assertThat(indicatorViewList).hasSize(5);
    }

    @Test
    void givenUnknownOrganization_shouldGedNoTerminalIndicators() {
        //GIVEN
        final String organization = "Unknown Organization";
        final String uid = "uid";

        final List<DigitalServiceTerminalIndicatorView> indicatorViewList = digitalServiceTerminalIndicatorRepository.findDigitalServiceTerminalIndicators(organization, uid);

        assertThat(indicatorViewList).isEmpty();
    }

    @Test
    void givenUnknownUid_shouldGedNoTerminalIndicators() {
        //GIVEN
        final String organization = "SSG";
        final String uid = "Unknown Uid";

        final List<DigitalServiceTerminalIndicatorView> indicatorViewList = digitalServiceTerminalIndicatorRepository.findDigitalServiceTerminalIndicators(organization, uid);

        assertThat(indicatorViewList).isEmpty();
    }
}
