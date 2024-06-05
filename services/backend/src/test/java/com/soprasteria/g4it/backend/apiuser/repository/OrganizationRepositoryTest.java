/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.common.utils.OrganizationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Sql("/sql/organization.sql")
@ActiveProfiles({"test"})
class OrganizationRepositoryTest {
    @Autowired
    private OrganizationRepository organizationRepository;

    public static String SUBSCRIBER = "SUBSCRIBER";
    public static String ORGANIZATION = "G4IT";
    public static Long SUBSCRIBER_ID = 1L;
    public static Long ORGANIZATION_ID = 1L;
    private static final List<String> activeStatus = List.of(OrganizationStatus.ACTIVE.name());

    @Test
    void findByIdAndSubscriberIdAndStatusIn() {
        assertThat(organizationRepository.findByIdAndSubscriberIdAndStatusIn(SUBSCRIBER_ID, ORGANIZATION_ID, activeStatus))
                .isPresent();
    }

    @Test
    void findBySubscriberIdAndName() {
        assertThat(organizationRepository.findBySubscriberIdAndName(SUBSCRIBER_ID, ORGANIZATION))
                .isPresent();
    }

    @Test
    void findAllByStatusIn() {
        assertThat(organizationRepository.findAllByStatusIn(activeStatus))
                .hasSize(2);
    }

    @Test
    void findByIdAndStatusIn() {
        assertThat(organizationRepository.findByIdAndStatusIn(ORGANIZATION_ID, activeStatus))
                .isPresent();
    }

}
