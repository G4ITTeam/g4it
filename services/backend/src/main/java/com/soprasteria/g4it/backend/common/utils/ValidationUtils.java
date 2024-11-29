/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import jakarta.validation.ConstraintViolation;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public final class ValidationUtils {
    private ValidationUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Get violations as string comma separated
     *
     * @param violations the violation set
     * @return the violation string or empty
     */
    public static Optional<String> getViolations(Set<ConstraintViolation<Object>> violations) {
        if (violations == null || violations.isEmpty()) return Optional.empty();

        return Optional.of(violations.stream()
                .map(v -> v.getPropertyPath().toString() + " " + v.getMessage())
                .sorted()
                .collect(Collectors.joining(", ")));
    }

}
