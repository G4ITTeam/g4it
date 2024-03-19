/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.scheduler;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class StorageDeletionScheduler {

    @Value("${g4it.storage.retention.oninit:false}")
    private Boolean onInit;

    @Autowired
    private StorageDeletionService storageAutoDeletionService;

    @PostConstruct
    public void init() {
        if (onInit) {
            storageAutoDeletionService.executeDeletion();
        }
    }

    /**
     * Execute the files deletion with a cron scheduler
     */
    @Scheduled(cron = "${g4it.storage.retention.cron}")
    public void executeAutoDeletion() {
        storageAutoDeletionService.executeDeletion();
    }

}
