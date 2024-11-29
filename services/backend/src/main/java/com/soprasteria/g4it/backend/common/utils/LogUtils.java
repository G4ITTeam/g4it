/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.utils;

import java.time.LocalDateTime;

public final class LogUtils {

    private LogUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Returns a string for printing log message info
     *
     * @param message the message
     * @return the log line
     */
    public static String info(final String message) {
        return String.format("%s - INFO - %s", LocalDateTime.now().format(Constants.LOCAL_DATE_TIME_FORMATTER), message);
    }

    /**
     * Returns a string for printing log message error
     *
     * @param message the message
     * @return the log line
     */
    public static String error(final String message) {
        return String.format("%s - ERROR - %s", LocalDateTime.now().format(Constants.LOCAL_DATE_TIME_FORMATTER), message);
    }
}
