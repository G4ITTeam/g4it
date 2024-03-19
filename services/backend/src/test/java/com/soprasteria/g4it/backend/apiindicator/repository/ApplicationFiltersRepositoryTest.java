/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.ApplicationFilters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Sql("/sql/application-indicator.sql")
class ApplicationFiltersRepositoryTest {

    @Autowired
    private ApplicationFiltersRepository applicationFiltersRepository;

    @Test
    void shouldGetFiltersByBatchName() {
        //Given
        final Long inventoryId = 501L;
        final String batchName = "44ad614e-94ad-46fb-b541-5053d8a45ee6";

        //When
        final List<ApplicationFilters> filters = applicationFiltersRepository.getFiltersByBatchName(inventoryId, batchName);

        //Then
        assertThat(filters).hasSize(16);
        assertThat(filters.stream().map(ApplicationFilters::getEnvironment).toList()).contains("Developpement", "Test");
        assertThat(filters.stream().map(ApplicationFilters::getLifeCycle).toList()).contains("DISTRIBUTION", "FABRICATION", "FIN_DE_VIE", "UTILISATION");
        assertThat(filters.stream().map(ApplicationFilters::getDomain).toList()).contains("Domain 1", "Domain 6");
        assertThat(filters.stream().map(ApplicationFilters::getType).toList()).contains("Personal Computer", "Server");
    }
}
