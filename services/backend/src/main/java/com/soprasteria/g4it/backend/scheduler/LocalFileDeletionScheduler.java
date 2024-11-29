/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class LocalFileDeletionScheduler {

    @Autowired
    private LocalFileDeletionService localFileDeletionService;

    /**
     * Execute the files deletion with a fixed rate
     */
    @Scheduled(fixedRateString = "${g4it.local.retention.ttl}")
    public void executeAutoDeletion() {
        localFileDeletionService.executeDeletion();
    }

}
