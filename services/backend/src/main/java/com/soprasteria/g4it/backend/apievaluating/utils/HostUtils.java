/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apievaluating.utils;

import java.util.Collections;
import java.util.List;

public class HostUtils {

    private HostUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Build the hosting efficiency value from mixElecQuartile and pue
     * Host Efficiency: take max between quartile and pue efficiency
     * returns 1 -> "Good"; 2 -> "Medium"; 3 -> "Bad".
     *
     * @param mixElecQuartile the mixElecQuartile
     * @param pue             the pue
     * @return Good, Medium or Bad
     */
    public static String buildHostingEfficiency(final Integer mixElecQuartile, final Double pue) {

        final Integer quartileEfficiency = switch (mixElecQuartile) {
            case 1 -> 1;
            case 2, 3 -> 2;
            default -> 3;
        };

        // Pue efficiency (<1,5 -> 1; 1.5 => x =>2,5 -> 2; >2.5 -> 3).
        final int pueEfficiency;
        if (pue < 1.5) {
            pueEfficiency = 1;
        } else if (pue <= 2.5) {
            pueEfficiency = 2;
        } else {
            pueEfficiency = 3;
        }

        final Integer hostEfficiencyValue = Collections.max(List.of(quartileEfficiency, pueEfficiency));
        return switch (hostEfficiencyValue) {
            case 1 -> "Good";
            case 2 -> "Medium";
            default -> "Bad";
        };
    }
}
