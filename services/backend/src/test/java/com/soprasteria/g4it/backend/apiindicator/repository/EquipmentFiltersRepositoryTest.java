/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.EquipmentFilters;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DataJpaTest
@Sql("/sql/indicatorview.sql")
@ActiveProfiles({"test"})
class EquipmentFiltersRepositoryTest {

    @Autowired
    private EquipmentFiltersRepository equipmentFiltersRepository;

    @Test
    void shouldReturnListOfFilters() {
        final List<EquipmentFilters> filters = this.equipmentFiltersRepository.getFiltersByInventoryId("SSG", 601L, "june-2022");
        assertAll(
                () -> assertThat(filters).hasSize(4),
                () -> assertThat(filters.get(1)).extracting(EquipmentFilters::getStatus).isEqualTo("actif"),
                () -> assertThat(filters.get(1)).extracting(EquipmentFilters::getEntity).isEqualTo("Sopra Steria Group"),
                () -> assertThat(filters.get(1)).extracting(EquipmentFilters::getType).isEqualTo("Compute"),
                () -> assertThat(filters.get(2)).extracting(EquipmentFilters::getCountry).isEqualTo("France"),
                () -> assertThat(filters.get(3)).extracting(EquipmentFilters::getEntity).isEqualTo("testEntite")
        );
    }

}
