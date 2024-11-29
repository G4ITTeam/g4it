/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import java.util.Locale;

public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Checks if a given string is in kebab-case format.
     *
     * @param value The string to check.
     * @return true if the string is in kebab-case
     */
    public static boolean isKebabCase(String value) {
        // loop over characters, and check if any char is not 'a-z0-9' and -
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (!((c >= 48 && c <= 57) || (c >= 97 && c <= 122) || c == 45)) {
                return false;
            }
        }

        return true;
    }

    /**
     * kebab-case string to SNAKE_CASE
     *
     * @param str string
     * @return the SNAKE_CASE string
     */
    public static String kebabToSnakeCase(final String str) {
        if (str == null) return null;
        return str.toUpperCase(Locale.getDefault()).replace('-', '_');
    }
}
