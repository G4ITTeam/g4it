/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.utils;

import java.time.format.DateTimeFormatter;

public final class Constants {

    /**
     * The progress percentage after completion.
     */
    public static final String COMPLETE_PROGRESS_PERCENTAGE = "100%";


    /**
     * The progress percentage when started.
     */
    public static final String STARTED_PROGRESS_PERCENTAGE = "0%";

    /**
     * DateTimeFormatter yyyyMMdd_HHmm
     */
    public static final DateTimeFormatter FORMATTER_DATETIME_MINUTE = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    /**
     * File url context key.
     */
    public static final String FILE_URL_CONTEXT_KEY = "file.url";

    /**
     * File length context key.
     */
    public static final String FILE_LENGTH_CONTEXT_KEY = "file.length";
}