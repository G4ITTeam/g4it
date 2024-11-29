/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

class ValidationUtilsTest {

    @Test
    void testGetViolations() {
        Assertions.assertEquals(Optional.empty(), ValidationUtils.getViolations(null));
        Assertions.assertEquals(Optional.empty(), ValidationUtils.getViolations(Set.of()));
    }


}
