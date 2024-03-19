/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.steps.common.config;

import com.soprasteria.g4it.backend.common.filesystem.model.FileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractFlagDataStepConfiguration {

    protected void skippedLinesCallback(final FileType fileType, final String line, final DelimitedLineTokenizer tokenizer, final FileMapperInfo fileInfo) {
        final List<String> potentialHeaders = new ArrayList<>(List.copyOf(fileInfo.getMapping(fileType).stream().map(Header::getName).toList()));
        final List<String> actualHeaders = Arrays.stream(line.split(";")).toList();
        final List<Integer> headersPosition = new ArrayList<>();

        final List<String> headers = new ArrayList<>();
        for (final String header : actualHeaders) {
            if (potentialHeaders.contains(header)) {
                headersPosition.add(actualHeaders.indexOf(header));
                headers.add(header);
            }
        }
        tokenizer.setNames(headers.toArray(String[]::new));
        tokenizer.setIncludedFields(headersPosition.stream().mapToInt(position -> position).toArray());
    }
}
