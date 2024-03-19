/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.upload.tasklet;

import com.soprasteria.g4it.backend.apibatchloading.exception.InventoryIntegrationRuntimeException;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Upload to the cloud Tasklet.
 */
@Slf4j
public class UploadResultTasklet implements Tasklet {

    /**
     * File url context key.
     */
    public static final String FILE_URL_CONTEXT_KEY = "file.url";

    /**
     * File length context key.
     */
    public static final String FILE_LENGTH_CONTEXT_KEY = "file.length";

    /**
     * Le système de gestion de fichier
     */
    private final FileStorage fileStorage;

    /**
     * The paths for files to upload
     */
    private final Path resultFilePath;
    /**
     * Folder for current session
     */
    private final String sessionPath;

    /**
     * If true, zip all files in single archive
     */
    private final boolean shouldZipResult;

    /**
     * Parametrized constructor for tasklet
     *
     * @param fileStorage    to upload files.
     * @param resultFilePath local path for the file to upload
     */
    public UploadResultTasklet(final FileStorage fileStorage, final Path resultFilePath, final String sessionPath, final boolean shouldZipResult) {
        this.fileStorage = fileStorage;
        this.resultFilePath = resultFilePath;
        this.sessionPath = sessionPath;
        this.shouldZipResult = shouldZipResult;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        if (!Files.exists(resultFilePath)) {
            log.warn("{} not found, skipping upload", resultFilePath);
            return RepeatStatus.FINISHED;
        }

        try (var paths = Files.newDirectoryStream(resultFilePath)) {
            if (shouldZipResult) {
                log.info("Creating archives");

                // Archive with all files and rejected files only
                final Path acceptedArchivePath = Path.of(resultFilePath.toFile().toString(), sessionPath + "_accepted.zip");
                final Path rejectedArchivePath = Path.of(resultFilePath.toFile().toString(), sessionPath + "_results.zip");
                final List<ZipEntrySource> rejectedEntries = new ArrayList<>();
                final List<ZipEntrySource> acceptedEntries = new ArrayList<>();
                paths.forEach(path -> {
                    if (path.toFile().getName().startsWith("rejected_")) {
                        Try.run(() -> rejectedEntries.add(new FileSource(path.getFileName().toString(), path.toFile())));
                    } else {
                        Try.run(() -> acceptedEntries.add(new FileSource(path.getFileName().toString(), path.toFile())));
                    }
                });
                ZipUtil.pack(rejectedEntries.toArray(new ZipEntrySource[0]), rejectedArchivePath.toFile());
                ZipUtil.pack(acceptedEntries.toArray(new ZipEntrySource[0]), acceptedArchivePath.toFile());
                uploadFile(rejectedArchivePath);
                uploadFile(acceptedArchivePath);

                stepContribution.getStepExecution().getExecutionContext().putString(FILE_URL_CONTEXT_KEY, fileStorage.getFileUrl(FileFolder.OUTPUT, Path.of(sessionPath, rejectedArchivePath.toFile().getName()).toString()));
                stepContribution.getStepExecution().getExecutionContext().putLong(FILE_LENGTH_CONTEXT_KEY, fileStorage.getFileSize(FileFolder.OUTPUT, Path.of(sessionPath, rejectedArchivePath.toFile().getName()).toString()));

                return RepeatStatus.FINISHED;
            }
            log.info("No archive will be generated. Uploading each file separately.");
            paths.forEach(path -> Try.run(() -> uploadFile(path)).getOrElseThrow(e -> new InventoryIntegrationRuntimeException(e.getMessage())));
        }
        return RepeatStatus.FINISHED;
    }

    private void uploadFile(final Path path) throws IOException {
        log.info("Uploading {}", path.toFile().getName());
        fileStorage.upload(path.toFile().getAbsolutePath(), FileFolder.OUTPUT, Path.of(sessionPath, path.toFile().getName()).toString());
    }

}
