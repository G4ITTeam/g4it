/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.utils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Stream;

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

    /**
     * Jwt email field
     */
    public static final String JWT_EMAIL_FIELD = "email";
    public static final String JWT_FIRST_NAME = "given_name";
    public static final String JWT_LAST_NAME = "family_name";
    public static final String JWT_SUB = "sub";
    public static final String ROLE_INVENTORY_READ = "ROLE_INVENTORY_READ";
    public static final String ROLE_INVENTORY_WRITE = "ROLE_INVENTORY_WRITE";
    public static final String ROLE_DIGITAL_SERVICE_READ = "ROLE_DIGITAL_SERVICE_READ";
    public static final String ROLE_DIGITAL_SERVICE_WRITE = "ROLE_DIGITAL_SERVICE_WRITE";
    public static final String ROLE_SUBSCRIBER_ADMINISTRATOR = "ROLE_SUBSCRIBER_ADMINISTRATOR";
    public static final String ROLE_ORGANIZATION_ADMINISTRATOR = "ROLE_ORGANIZATION_ADMINISTRATOR";

    public static final List<String> ALL_BASIC_ROLES = List.of(
            ROLE_DIGITAL_SERVICE_WRITE,
            ROLE_DIGITAL_SERVICE_READ,
            ROLE_INVENTORY_WRITE,
            ROLE_INVENTORY_READ
    );

    public static final List<String> ALL_ROLES = Stream.concat(
                    Stream.of(ROLE_SUBSCRIBER_ADMINISTRATOR, ROLE_ORGANIZATION_ADMINISTRATOR),
                    ALL_BASIC_ROLES.stream())
            .toList();

    public static final String DEMO = "DEMO";
    public static final Long INTERNAL_ORGANIZATION = 0L;
    public static final String INTERNAL_SUBSCRIBER = "--INTERNAL-G4IT--";

    public static final List<String> STATUS_IN_PROGRESS = List.of(
            ExportBatchStatus.LOADING_SIP_REFERENTIAL.name(),
            ExportBatchStatus.EXPORTING_DATA.name(),
            ExportBatchStatus.UPLOADING_DATA.name(),
            ExportBatchStatus.CLEANING_WORKING_FOLDERS.name()
    );

    public final static List<String> ORGANIZATION_ACTIVE_OR_DELETED_STATUS = List.of(
            OrganizationStatus.ACTIVE.name(),
            OrganizationStatus.TO_BE_DELETED.name()
    );

    private Constants() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}