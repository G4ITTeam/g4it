/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.utils;

/**
 * Utils class for criteria.
 */
public final class CriteriaUtils {

    /**
     * Private constructor to hive the implicit public one.
     */
    private CriteriaUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Method to transform criteria label to criteria key.
     *
     * @param criteriaName the criteria label.
     * @return the criteria key.
     */
    public static String transformCriteriaNameToCriteriaKey(final String criteriaName) {
        return switch (criteriaName) {
            case "Climate change" -> "climate-change";
            case "Ionising radiation" -> "ionising-radiation";
            case "Acidification" -> "acidification";
            case "Particulate matter and respiratory inorganics" -> "particulate-matter";
            case "Resource use (minerals and metals)" -> "resource-use";
            case "Ozone depletion" -> "ozone-depletion";
            case "Photochemical ozone formation" -> "photochemical-ozone-formation";
            case "Eutrophication, terrestrial" -> "eutrophication-terrestrial";
            case "Eutrophication, freshwater" -> "eutrophication-freshwater";
            case "Eutrophication, marine" -> "eutrophication-marine";
            case "Resource use, fossils" -> "resource-use-fossils";
            default -> "";
        };
    }

    /**
     * Method to transform criteria key to criteria label.
     *
     * @param criteriaKey the criteria key.
     * @return the criteria label.
     */
    public static String transformCriteriaKeyToCriteriaName(final String criteriaKey) {
        return switch (criteriaKey) {
            case "climate-change" -> "Climate change";
            case "ionising-radiation" -> "Ionising radiation";
            case "acidification" -> "Acidification";
            case "particulate-matter" -> "Particulate matter and respiratory inorganics";
            case "resource-use" -> "Resource use (minerals and metals)";
            case "ozone-depletion" -> "Ozone depletion";
            case "photochemical-ozone-formation" -> "Photochemical ozone formation";
            case "eutrophication-terrestrial" -> "Eutrophication, terrestrial";
            case "eutrophication-freshwater" -> "Eutrophication, freshwater";
            case "eutrophication-marine" -> "Eutrophication, marine";
            case "resource-use-fossils" -> "Resource use, fossils";
            default -> "";
        };
    }

}
