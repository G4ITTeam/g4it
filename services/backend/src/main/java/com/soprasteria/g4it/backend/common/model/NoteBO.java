package com.soprasteria.g4it.backend.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NoteBO {
    /**
     * Unique identifier.
     */
    private String id;

    /**
     * Name.
     */
    private String content;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Last update date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime lastUpdateDate;

}
