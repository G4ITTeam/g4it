/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.external.numecoeval.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Num Eco Eval Input Data Exposition report.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "numecoeval_input_report")
public class NumEcoEvalInputReport extends AbstractBaseEntity implements Serializable {

    /**
     * Auto Generated Identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The unique numEcoEval identifier for inputs.
     */
    private String batchName;

    /**
     * Report Date.
     */
    private Date reportDate;

    /**
     * Input file.
     */
    @Size(max = 255, message = "Le nom du fichier doit faire moins de 255 caractères")
    private String file;

    /**
     * Errors during upload input data.
     */
    @ElementCollection
    @CollectionTable(name = "numecoeval_input_error_report", joinColumns = @JoinColumn(name = "numecoeval_report_id"))
    @Column(name = "error")
    private List<String> errors;

    /**
     * Import line number.
     */
    private Integer importLinesNumber;

}
