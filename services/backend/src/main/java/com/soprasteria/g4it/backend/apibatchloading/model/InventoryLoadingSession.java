/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.model;

import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Data
@Builder

public class InventoryLoadingSession {

    public static final String SESSION_PATH_FORMAT = "yyyyMMdd-HHmmss";

    /**
     * Session date for the job.
     * Used to identify uniquely job
     */
    private final Date sessionDate;

    /**
     * Input form containing files to process
     */
    private final List<FileDescription> files;

    /**
     * Subscriber.
     */
    private String subscriber;

    /**
     * Organization on which the job should operate
     */
    private final String organization;

    /**
     * The inventory id.
     */
    @Setter
    private Long inventoryId;

    /**
     * The name of the inventory
     */
    @Setter
    private String inventoryName;

    /**
     * The locale to use to validate data.
     */
    private Locale locale;

    public String getSessionPath() {
        return new SimpleDateFormat(SESSION_PATH_FORMAT).format(sessionDate);
    }
}
