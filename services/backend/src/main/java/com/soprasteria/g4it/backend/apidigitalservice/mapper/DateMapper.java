/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.mapper;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * Date Mapper.
 */
@Mapper(componentModel = "spring")
public class DateMapper {

    /**
     * Map LocalDateTime to OffsetDateTime.
     *
     * @param value the LocalDateTime to convert.
     * @return the converted date.
     */
    public OffsetDateTime mapOffsetDateTime(final LocalDateTime value) {
        return Optional.ofNullable(value).map(date -> date.atOffset(ZoneOffset.UTC)).orElse(null);
    }

    /**
     * Map OffsetDateTime to LocalDateTime.
     *
     * @param value the OffsetDateTime to convert.
     * @return the converted date.
     */
    public LocalDateTime mapLocalDateTime(final OffsetDateTime value) {
        return Optional.ofNullable(value).map(OffsetDateTime::toLocalDateTime).orElse(null);
    }
}
