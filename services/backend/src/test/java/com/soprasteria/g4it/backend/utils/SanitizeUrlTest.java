/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.utils;

import com.soprasteria.g4it.backend.common.utils.SanitizeUrl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SanitizeUrlTest {

    @Test
    void testSanitizeUrl_nothingReplaced() {
        assertEquals("https://test.com", SanitizeUrl.removeTokenAndEncoding("https://test.com"));
    }

    @Test
    void testSanitizeUrl_replaceEncodingAndTruncate() {
        assertEquals("https://test.com/path/to", SanitizeUrl.removeTokenAndEncoding("https://test.com%5Cpath%2Fto?param=value"));
    }
}
