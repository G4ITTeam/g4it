/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apibatchexport.tasklet;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apibatchexport.exception.ExportRuntimeException;
import com.soprasteria.g4it.backend.common.filesystem.business.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.ExportBatchStatus;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.zeroturnaround.zip.FileSource;
import org.zeroturnaround.zip.ZipEntrySource;
import org.zeroturnaround.zip.ZipUtil;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Upload to the cloud Tasklet.
 */
@Slf4j
@AllArgsConstructor
public class UploadExportResultTasklet implements Tasklet {


    /**
     * The file storage.
     */
    private final FileStorage fileStorage;

    /**
     * The paths for files to upload.
     */
    private final Path resultFilePath;

    /**
     * The inventory name.
     */
    private final long inventoryId;

    /**
     * Batch Name
     */
    private final String batchName;

    /**
     * Export Service to update batch status
     */
    private InventoryExportService inventoryExportService;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws Exception {
        inventoryExportService.updateBatchStatusCode(batchName, ExportBatchStatus.UPLOADING_DATA.name());

        if (!Files.exists(resultFilePath)) {
            log.warn("{} not found, skipping upload", resultFilePath);
            return RepeatStatus.FINISHED;
        }

        final Path exportArchivePath = Try.withResources(() -> Files.newDirectoryStream(resultFilePath))
                .of(this::uploadFile)
                .map(path -> Path.of(path.toFile().getName()))
                .getOrElseThrow(e -> new ExportRuntimeException("Upload exception", e));
        stepContribution.getStepExecution().getJobExecution().getExecutionContext().putString(Constants.FILE_URL_CONTEXT_KEY, fileStorage.getFileUrl(FileFolder.EXPORT, Path.of(exportArchivePath.toFile().getName()).toString()));
        stepContribution.getStepExecution().getJobExecution().getExecutionContext().putLong(Constants.FILE_LENGTH_CONTEXT_KEY, fileStorage.getFileSize(FileFolder.EXPORT, Path.of(exportArchivePath.toFile().getName()).toString()));

        return RepeatStatus.FINISHED;
    }

    private Path uploadFile(final DirectoryStream<Path> paths) throws IOException {
        final Path exportArchivePath = Path.of(
                resultFilePath.toFile().toString(),
                String.format("%s_%s.zip", LocalDateTime.now().format(Constants.FORMATTER_DATETIME_MINUTE), inventoryId)
        );
        final List<ZipEntrySource> entries = new ArrayList<>();
        paths.forEach(path -> Try.run(() -> entries.add(new FileSource(path.getFileName().toString(), path.toFile()))));
        ZipUtil.pack(entries.toArray(new ZipEntrySource[0]), exportArchivePath.toFile());
        fileStorage.upload(exportArchivePath.toFile().getAbsolutePath(), FileFolder.EXPORT, Path.of(exportArchivePath.toFile().getName()).toString());
        return exportArchivePath;
    }

}
