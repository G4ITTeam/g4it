/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.external.numecoeval.business;

import com.soprasteria.g4it.backend.external.numecoeval.client.ReferentialClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

@Profile("!test")
@Service
@Slf4j
public class InitLoadRefService {

    /**
     * ReferentialClient
     */
    @Autowired
    private ReferentialClient referentialClient;

    /**
     * Storagetmp path
     */
    @Value("${local.working.folder}")
    private String storagetmp;

    /**
     * On init of component, set referential if table is empty
     *
     * @throws IOException cannot read file
     */
    @PostConstruct
    void initReferentialIfEmpty() throws IOException {
        try {
            var criteria = referentialClient.getAllCriteria();

            if (!(criteria == null || criteria.isEmpty())) {
                return;
            }
        } catch (Exception e) {
            log.error("Cannot connect to referential api", e);
            return;
        }

        log.info("Start Import referential files into NumEcoEval referential");
        referentialClient.importLifecycleSteps(readRefFile("lifecycleSteps.csv"));
        referentialClient.importHypotheses(readRefFile("hypotheses.csv"));
        referentialClient.importCriteria(readRefFile("criteria.csv"));
        referentialClient.importEquipmentRefMatching(readRefFile("equipmentRefMatching.csv"));
        referentialClient.importItemTypes(readRefFile("itemTypes.csv"));
        referentialClient.importCharacterizationFactors(readRefFile("characterizationFactors.csv"));
        log.info("End Import referential files into NumEcoEval referential");

    }

    /**
     * Read referential file and create a File
     *
     * @param filename the referential filename
     * @return the File
     * @throws IOException if not found
     */
    private File readRefFile(String filename) throws IOException {
        InputStream is = new ClassPathResource("referential/numecoeval/" + filename).getInputStream();
        File file = Path.of(storagetmp, "tmp", "ref.csv").toFile();
        copyInputStreamToFile(is, file);
        return file;
    }

}
