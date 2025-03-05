/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.utils;

import java.util.Map;

public final class LifecycleStepUtils {

    private static final Map<String, String> LIFECYCLE_TRANSLATE = Map.of(
            "FABRICATION", "MANUFACTURING",
            "DISTRIBUTION", "TRANSPORTATION",
            "UTILISATION", "USING",
            "FIN_DE_VIE", "END_OF_LIFE"
    );

    private static final Map<String, String> LIFECYCLE_TRANSLATE_REVERSED = Map.of(
            "MANUFACTURING", "FABRICATION",
            "TRANSPORTATION", "DISTRIBUTION",
            "USING", "UTILISATION",
            "END_OF_LIFE", "FIN_DE_VIE"
    );

    /**
     * Private constructor to hive the implicit public one.
     */

    private LifecycleStepUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Method to translate lifecycle step from old referential to the new one
     *
     * @param lifecycleStep the lifecycle step.
     * @return the translated lifecycle step
     */
    public static String get(final String lifecycleStep) {
        if (lifecycleStep == null) return null;
        return LIFECYCLE_TRANSLATE.getOrDefault(lifecycleStep, "");
    }

    /**
     * Method to translate lifecycle step from old referential to the new one
     *
     * @param lifecycleStep the lifecycle step.
     * @param defaultValue  the default value.
     * @return the translated lifecycle step
     */
    public static String get(final String lifecycleStep, final String defaultValue) {
        if (lifecycleStep == null) return null;
        return LIFECYCLE_TRANSLATE.getOrDefault(lifecycleStep, defaultValue);
    }

    /**
     * Method to translate lifecycle step from new referential to the old one
     *
     * @param lifecycleStep the lifecycle step.
     * @return the translated lifecycle step
     */
    public static String getReverse(final String lifecycleStep) {
        if (lifecycleStep == null) return null;
        return LIFECYCLE_TRANSLATE_REVERSED.getOrDefault(lifecycleStep, lifecycleStep);
    }
}
