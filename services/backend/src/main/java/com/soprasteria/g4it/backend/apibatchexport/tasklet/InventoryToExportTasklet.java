/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.tasklet;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.apiinventory.repository.InventoryRepository;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Remove working folder Tasklet
 */
@Slf4j
@AllArgsConstructor
public class InventoryToExportTasklet implements Tasklet {

    /**
     * Path to working Folder
     */
    private final String localWorkingFolder;

    /**
     * Header list.
     */
    private final List<String> headers;

    /**
     * The inventory identifier.
     */
    private final Long inventoryId;

    /**
     * The repository to access inventory data.
     */
    private final InventoryRepository inventoryRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        final Inventory inventory = inventoryRepository.findById(inventoryId).orElseThrow();
        final File csvOutputFile = new File(Path.of(localWorkingFolder, "inventory.csv").toString());
        final List<String> dataLines = new ArrayList<>();
        dataLines.add(String.join(";", headers));
        dataLines.add(String.join(";",
                inventory.getId().toString(),
                inventory.getName(),
                inventory.getOrganization().getName(),
                inventory.getCreationDate().format(DateTimeFormatter.ISO_DATE),
                inventory.getLastUpdateDate().format(DateTimeFormatter.ISO_DATE),
                inventory.getDataCenterCount().toString(),
                inventory.getPhysicalEquipmentCount().toString(),
                inventory.getVirtualEquipmentCount().toString(),
                inventory.getApplicationCount().toString()));
        Try.withResources(() -> new PrintWriter(csvOutputFile)).of(pw -> write(pw, dataLines));
        return RepeatStatus.FINISHED;
    }

    /**
     * Write all lines in inventory file.
     *
     * @param printWriter the writer.
     * @param lines       lines to write.
     * @return return always trye.
     */
    private boolean write(final PrintWriter printWriter, final List<String> lines) {
        lines.forEach(printWriter::println);
        return true;
    }

}
