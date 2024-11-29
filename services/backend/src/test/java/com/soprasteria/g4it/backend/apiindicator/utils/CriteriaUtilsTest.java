/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.utils;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class CriteriaUtilsTest {

    @Test
    void testCriteriaUtils_transformCriteriaNameToCriteriaKey() {
        Assertions.assertEquals("climate-change", CriteriaUtils.transformCriteriaNameToCriteriaKey("Climate change"));
        Assertions.assertEquals("", CriteriaUtils.transformCriteriaNameToCriteriaKey("Unknown"));
    }

    @Test
    void testCriteriaUtils_transformCriteriaKeyToCriteriaName() {
        Assertions.assertEquals("Climate change", CriteriaUtils.transformCriteriaKeyToCriteriaName("climate-change"));
        Assertions.assertEquals("", CriteriaUtils.transformCriteriaNameToCriteriaKey("Unknown"));
    }

}
