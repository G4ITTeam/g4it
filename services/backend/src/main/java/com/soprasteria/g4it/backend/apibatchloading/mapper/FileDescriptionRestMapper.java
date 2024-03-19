/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchloading.mapper;

import com.soprasteria.g4it.backend.common.filesystem.model.FileDescription;
import com.soprasteria.g4it.backend.common.filesystem.model.FileType;
import com.soprasteria.g4it.backend.server.gen.api.dto.FileDescriptionRest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FileDescriptionRestMapper {

    FileDescriptionRest toDto(final FileDescription bo);

    @Mapping(target = "type", source = "type.value")
    List<FileDescriptionRest> toDto(final List<FileDescription> bo);

    FileDescription toBusinessObject(final FileDescriptionRest source);

    List<FileDescription> toBusinessObject(final List<FileDescriptionRest> source);

    default FileType map(final String type) {
        return FileType.fromValue(type);
    }
}
