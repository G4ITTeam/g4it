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

class StringUtilsTest {

    @Test
    void testIsKebabCase() {
        Assertions.assertTrue(StringUtils.isKebabCase("is-kebab-case"));
        Assertions.assertTrue(StringUtils.isKebabCase("0123456789-abcyz-x"));
        Assertions.assertFalse(StringUtils.isKebabCase("is_not_kebab_case"));
        Assertions.assertFalse(StringUtils.isKebabCase("is not kebab case"));
        Assertions.assertFalse(StringUtils.isKebabCase("IS-NOT-KEBAB-CASE"));
    }

    @Test
    void testKebabCaseToSnakeCase() {
        Assertions.assertEquals("STR_TO_CHANGE", StringUtils.kebabToSnakeCase("str-to-change"));
        Assertions.assertEquals("", StringUtils.kebabToSnakeCase(""));
        Assertions.assertNull(StringUtils.kebabToSnakeCase(null));
    }
}
