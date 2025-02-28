/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.common.filesystem.business.local;

import com.soprasteria.g4it.backend.common.filesystem.model.CsvFileMapperInfo;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.common.filesystem.model.Header;
import com.soprasteria.g4it.backend.common.utils.Constants;
import com.soprasteria.g4it.backend.common.utils.CsvUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class CsvFileService {

    @Autowired
    private CsvFileMapperInfo csvFileMapperInfo;

    /**
     * Get csv printer for a fileType and output in directory
     *
     * @param fileType  the fileType
     * @param directory the output directory
     * @return the csv printer
     * @throws IOException local file creation exception
     */
    public CSVPrinter getPrinter(FileType fileType, Path directory) throws IOException {

        return new CSVPrinter(new FileWriter(
                directory.resolve(fileType.getFileName() + Constants.CSV).toFile()
        ), CSVFormat.Builder.create()
                .setHeader(csvFileMapperInfo.getMapping(fileType).stream()
                        .map(Header::getName).toArray(String[]::new))
                .setDelimiter(CsvUtils.DELIMITER)
                .build());
    }

}
