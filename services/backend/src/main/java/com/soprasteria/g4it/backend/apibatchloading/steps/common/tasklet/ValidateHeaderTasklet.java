/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.tasklet;

import com.soprasteria.g4it.backend.apibatchloading.exception.InventoryLoadingException;
import com.soprasteria.g4it.backend.common.filesystem.model.FileFolder;
import com.soprasteria.g4it.backend.common.filesystem.model.FileStorage;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.exception.InvalidHeaderException;
import io.vavr.control.Try;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

/**
 * Header validation tasklet.
 */
@AllArgsConstructor
public class ValidateHeaderTasklet implements Tasklet {

    public static final String SKIPPED_EXIT_STATUS = "SKIPPED";

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidateHeaderTasklet.class);

    /**
     * The file storage to access files
     */
    private final FileStorage fileStorage;

    /**
     * The file type.
     */
    private FileType fileType;

    /**
     * The session date formatted.
     */
    private final String sessionPath;

    /**
     * Headers of file to validate.
     */
    private final List<Header> expectedHeaders;

    /**
     * {@inheritDoc}
     */
    @Override
    public RepeatStatus execute(final StepContribution stepContribution, final ChunkContext chunkContext) throws InventoryLoadingException, IOException {
        final Resource[] inputs = Try.of(() -> fileStorage.listResources(FileFolder.WORK, sessionPath, fileType))
                .getOrElseThrow(error -> new InventoryLoadingException("Error to list " + fileType.name() + " working file", error));
        int headerErrorNumber = 0;
        for (final Resource inputResource : inputs) {
            try {
                validateHeaders(inputResource);
            } catch (final InvalidHeaderException e) {
                headerErrorNumber++;
                LOGGER.warn(e.getMessage());
            }
        }
        if (headerErrorNumber == inputs.length) {
            stepContribution.getStepExecution().setExitStatus(new ExitStatus(SKIPPED_EXIT_STATUS));
        }
        return RepeatStatus.FINISHED;
    }

    /**
     * Method to validate headers.
     *
     * @param inputFile the input file to validate.
     * @throws InventoryLoadingException when error occurs while reading file.
     * @throws InvalidHeaderException    when headers aren't valid.
     */
    private void validateHeaders(final Resource inputFile) throws InventoryLoadingException, InvalidHeaderException {
        final String firstLine = Try.withResources(() -> new BufferedReader(new InputStreamReader(inputFile.getInputStream())))
                .of(BufferedReader::readLine)
                .getOrElseThrow(error -> new InventoryLoadingException("Error to access " + fileType.name() + " working file", error));

        final List<String> headers = new ArrayList<>(List.of(firstLine.split(";")));

        final List<String> mandatoryHeaders = expectedHeaders.stream().filter(Predicate.not(Header::isOptional)).map(Header::getName).toList();
        if (!new HashSet<>(headers).containsAll(mandatoryHeaders)) {
            throw new InvalidHeaderException(String.format("Missing column in file %s. %s are mandatory", inputFile.getFilename(), String.join(", ", mandatoryHeaders)));
        }
        headers.removeAll(mandatoryHeaders);

        final List<String> optionalHeaders = expectedHeaders.stream().filter(Header::isOptional).map(Header::getName).toList();
        headers.removeAll(optionalHeaders);

        if (!headers.isEmpty()) {
            LOGGER.warn("Columns {} are unknown.", String.join(", ", headers));
        }
    }

}
