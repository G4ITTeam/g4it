/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apireferential.business;

import com.soprasteria.g4it.backend.apiindicator.utils.CriteriaUtils;
import com.soprasteria.g4it.backend.apiindicator.utils.LifecycleStepUtils;
import com.soprasteria.g4it.backend.apireferential.mapper.ReferentialMapper;
import com.soprasteria.g4it.backend.apireferential.modeldb.*;
import com.soprasteria.g4it.backend.apireferential.persistence.ReferentialPersistenceService;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import com.soprasteria.g4it.backend.common.utils.StringUtils;
import com.soprasteria.g4it.backend.external.numecoeval.business.NumEcoEvalReferentialRemotingService;
import com.soprasteria.g4it.backend.server.gen.api.dto.HypothesisRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.ItemImpactRest;
import com.soprasteria.g4it.backend.server.gen.api.dto.MatchingItemRest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
@Slf4j
@Profile("!test")
public class ReferentialMigrationService {

    private static final String VALEUR = "valeur";
    @Autowired
    NumEcoEvalReferentialRemotingService numEcoEvalReferentialRemotingService;
    @Autowired
    ReferentialPersistenceService saveService;
    @Autowired
    ReferentialMapper referentialMapper;
    @Value("${g4it.referential.migration}")
    private boolean isMigrationEnabled;
    @Value("${num-eco-eval-referential.base-url}")
    private String numEcoEvalBaseUrl;
    @Value("${local.working.folder}")
    private String localWorkingFolder;

    @EventListener(ApplicationReadyEvent.class)
    public void migrateReferentialData() {
        if (!isMigrationEnabled) {
            log.info("Referential migration is disabled");
            return;
        }

        log.info("Starting referential data migration");

        Path workdir = Path.of(localWorkingFolder).resolve("referentialMigration");

        try {
            Files.createDirectories(workdir);
        } catch (IOException e) {
            log.error("Cannot create directory {}", workdir, e);
            return;
        }

        // Migrate criteria
        List<Criterion> criterionEntities = numEcoEvalReferentialRemotingService.getCriteriaList().stream()
                .map(critereDTO -> {
                    var criterionRest = referentialMapper.criteriaDtoToRest(critereDTO);

                    String translatedCode = StringUtils.kebabToSnakeCase(CriteriaUtils.transformCriteriaNameToCriteriaKey(criterionRest.getLabel()));
                    criterionRest.setCode(translatedCode);

                    if (translatedCode.isEmpty()) {
                        log.warn("No translation found for criteria {}", criterionRest.getLabel());
                        return null;
                    } else {
                        return referentialMapper.toEntity(criterionRest);
                    }

                })
                .filter(Objects::nonNull)
                .toList();

        int size = saveService.saveCriteria(criterionEntities);
        log.info("Migrated {} criteria", size);

        // Migrate lifecycle steps
        List<LifecycleStep> lifecycleEntities = numEcoEvalReferentialRemotingService.getLifecycleSteps().stream()
                .map(lifecycleDto -> {
                    var lifecyle = referentialMapper.lifecycleDtoToRest(lifecycleDto);
                    lifecyle.setCode(LifecycleStepUtils.get(lifecyle.getCode()));
                    return referentialMapper.toEntity(lifecyle);
                })
                .toList();

        size = saveService.saveLifecycleSteps(lifecycleEntities);
        log.info("Migrated {} lifecycle steps", size);

        // Migrate item types
        List<ItemType> itemTypeEntities = numEcoEvalReferentialRemotingService.getItemTypes().stream()
                .map(typeItemDTO -> referentialMapper.toEntity(referentialMapper.itemTypeDtoToRest(typeItemDTO)))
                .toList();

        size = saveService.saveItemTypes(itemTypeEntities);
        log.info("Migrated {} item types", size);

        migrateHypotheses(workdir);
        migrateItemMatching(workdir);
        migrateItemImpacts(workdir);

        try {
            FileSystemUtils.deleteRecursively(workdir);
            log.info("Referential data migration completed successfully. Any changes in new referential will be overridden on next start-up");
        } catch (IOException e) {
            log.error("Error while waiting on or on deleting folder: {}", workdir, e);
        }
    }

    /**
     * Migrate hypotheses from CSV API
     *
     * @param workdir the work dir
     */
    private void migrateHypotheses(Path workdir) {
        File file = exportReferentialCsv(workdir, "hypotheses");

        try (Reader reader = new FileReader(file)) {

            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CsvUtils.DELIMITER)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .build().parse(reader);

            List<Hypothesis> objects = records.stream()
                    .map((CSVRecord csvRecord) -> {
                        HypothesisRest hypothesisRest = new HypothesisRest();
                        hypothesisRest.setCode(csvRecord.get("cle"));
                        hypothesisRest.setValue(Double.valueOf(csvRecord.get(VALEUR)));
                        hypothesisRest.setSource(csvRecord.get("source"));
                        hypothesisRest.setDescription(csvRecord.get("description"));

                        return referentialMapper.toEntity(hypothesisRest);
                    })
                    .toList();

            int size = saveService.saveHypotheses(objects);

            log.info("Migrated {} hypotheses", size);
        } catch (IOException e) {
            log.error("Error during referential data migration of hypothesis", e);
        }
    }

    /**
     * Migrate item matching from CSV API
     *
     * @param workdir the work dir
     */
    private void migrateItemMatching(Path workdir) {
        File file = exportReferentialCsv(workdir, "correspondanceRefEquipement");

        try (Reader reader = new FileReader(file)) {

            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CsvUtils.DELIMITER)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .build().parse(reader);

            List<MatchingItem> objects = records.stream()
                    .map((CSVRecord csvRecord) -> {
                        MatchingItemRest matchingItemRest = new MatchingItemRest();

                        try {
                            matchingItemRest.setItemSource(csvRecord.get("modeleEquipementSource"));
                            matchingItemRest.setRefItemTarget(csvRecord.get("refEquipementCible"));
                        } catch (IllegalArgumentException e) {
                            matchingItemRest.setItemSource(csvRecord.get("cle"));
                            matchingItemRest.setRefItemTarget(csvRecord.get(VALEUR));
                        }

                        return referentialMapper.toEntity(matchingItemRest);
                    })
                    .toList();

            int size = saveService.saveItemMatchings(objects);

            log.info("Migrated {} matching items", size);
        } catch (IOException e) {
            log.error("Error during referential data migration of item matching", e);
        }
    }

    /**
     * Migrate item impacts from CSV API
     * Use pagination mode for optimization
     *
     * @param workdir the work dir
     */
    private void migrateItemImpacts(Path workdir) {
        saveService.truncateItemImpacts();

        File file = exportReferentialCsv(workdir, "facteursCaracterisation");

        Set<String> criteriaNotMigrated = new HashSet<>();
        Set<String> lifecycleStepNotMigrated = new HashSet<>();

        try (Reader reader = new FileReader(file)) {

            CSVParser records = CSVFormat.RFC4180.builder()
                    .setHeader()
                    .setDelimiter(CsvUtils.DELIMITER)
                    .setAllowMissingColumnNames(true)
                    .setSkipHeaderRecord(false)
                    .build().parse(reader);

            int row = 1;
            int pageNumber = 0;

            List<ItemImpact> objects = new ArrayList<>(Constants.BATCH_SIZE);

            for (CSVRecord csvRecord : records) {

                boolean hasMissingRef = false;

                ItemImpactRest itemImpactRest = new ItemImpactRest();
                itemImpactRest.setName(csvRecord.get("nom"));
                itemImpactRest.setLifecycleStep(LifecycleStepUtils.get(csvRecord.get("etapeacv")));
                if ("".equals(itemImpactRest.getLifecycleStep())) {
                    lifecycleStepNotMigrated.add(csvRecord.get("etapeacv"));
                    hasMissingRef = true;
                }

                itemImpactRest.setCriterion(StringUtils.kebabToSnakeCase(CriteriaUtils.transformCriteriaNameToCriteriaKey(csvRecord.get("critere"))));
                if ("".equals(itemImpactRest.getCriterion())) {
                    criteriaNotMigrated.add(csvRecord.get("critere"));
                    hasMissingRef = true;
                }

                if (hasMissingRef) continue;

                itemImpactRest.setDescription(csvRecord.get("description"));
                itemImpactRest.setLevel(csvRecord.get("niveau"));
                itemImpactRest.setTier(csvRecord.get("tiers"));
                itemImpactRest.setCategory(csvRecord.get("categorie"));
                itemImpactRest.setAvgElectricityConsumption(CsvUtils.readDouble(csvRecord, "consoElecMoyenne"));
                itemImpactRest.setLocation(csvRecord.get("localisation"));
                itemImpactRest.setValue(CsvUtils.readDouble(csvRecord, VALEUR));
                itemImpactRest.setUnit(csvRecord.get("unite"));
                itemImpactRest.setSource(csvRecord.get("source"));

                objects.add(referentialMapper.toEntity(itemImpactRest));
                if (row >= Constants.BATCH_SIZE) {
                    saveService.saveItemImpacts(objects);
                    objects.clear();
                    row = 1;
                    pageNumber++;
                } else {
                    row++;
                }
            }

            saveService.saveItemImpacts(objects);

            log.info("Migrated {} item impacts", pageNumber * Constants.BATCH_SIZE + row - 1);
            if (!criteriaNotMigrated.isEmpty()) {
                log.warn("Some item impacts has not been migrated because no translation found for criterion: {}", String.join(", ", criteriaNotMigrated));
            }
            if (!lifecycleStepNotMigrated.isEmpty()) {
                log.warn("Some item impacts has not been migrated because no translation found for lifecyle steps: {}", String.join(", ", lifecycleStepNotMigrated));
            }
        } catch (IOException e) {
            log.error("Error during referential data migration of item impact", e);
        }

    }

    /**
     * Get the NumEcoEval exported referential by calling GET /referentiel/{type}/csv
     *
     * @param workdir         the work directory
     * @param referentialName the referential name
     * @return the downloaded File
     */
    private File exportReferentialCsv(Path workdir, String referentialName) {
        Path outputFile = workdir.resolve(referentialName + ".csv");

        try (InputStream in = new URI(numEcoEvalBaseUrl + "/referentiel/" + referentialName + "/csv").toURL().openStream()) {
            Files.copy(in, outputFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | URISyntaxException e) {
            log.error("Cannot download csv file from API /referentiel/{}/csv", referentialName, e);
        }

        return outputFile.toFile();
    }
}
