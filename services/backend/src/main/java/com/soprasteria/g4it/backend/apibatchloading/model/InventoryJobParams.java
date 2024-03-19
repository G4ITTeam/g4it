/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.model;

import lombok.Builder;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;

import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

@Builder
public class InventoryJobParams {

    /**
     * Inventory Identifier job param identifier.
     */
    public static final String INVENTORY_ID_JOB_PARAM = "inventory.id";
    public static final String BATCH_NAME_JOB_PARAM = "batch.name";
    public static final String INVENTORY_NAME_JOB_PARAM = "inventory.name";
    public static final String SUBSCRIBER_JOB_PARAM = "subscriber";
    public static final String ORGANIZATION_JOB_PARAM = "organization";

    /**
     * The client subscriber.
     */
    private String subscriber;

    /**
     * The subscriber's organisation.
     */
    private final String organization;
    /**
     * Inventory Id
     */
    private final long inventoryId;

    private final String inventoryName;

    private final Date sessionDate;

    private final Locale locale;

    /**
     * Base path for the batch local working folder
     * Default to batch current working directory
     */
    @Builder.Default
    private final String localWorkingFolderBasePath = "";
    /**
     * Toggles if local working folder should be deleted.
     * Default = true
     */
    @Builder.Default
    private final boolean deleteLocalWorkingFolder = true;
    /**
     * Toggles if results should be gathered in a zip file.
     * Default = true
     */
    @Builder.Default
    private final boolean zipResults = true;

    /**
     * Transform attribute to jobParameters.
     *
     * @return spring batch job parameters.
     */
    public JobParameters toJobParams() {
        return new JobParametersBuilder()
                .addDate("session.date", sessionDate, true)
                .addString(SUBSCRIBER_JOB_PARAM, subscriber)
                .addString(ORGANIZATION_JOB_PARAM, organization)
                .addLong(INVENTORY_ID_JOB_PARAM, inventoryId)
                .addString("local.working.folder", genRandomFolderName())
                .addString("delete.local.working.folder", Boolean.toString(deleteLocalWorkingFolder))
                .addString("zip.results", Boolean.toString(zipResults))
                .addString(INVENTORY_NAME_JOB_PARAM, inventoryName)
                .addString(BATCH_NAME_JOB_PARAM, UUID.randomUUID().toString())
                .addJobParameter("locale", locale, Locale.class)
                .toJobParameters();
    }

    private String genRandomFolderName() {
        // We generate random folder name
        return Paths.get(localWorkingFolderBasePath, UUID.randomUUID().toString()).toString();
    }

}
