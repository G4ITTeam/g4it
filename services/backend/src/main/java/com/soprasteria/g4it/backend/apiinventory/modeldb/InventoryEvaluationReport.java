/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiinventory.modeldb;

import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalCalculationReport;
import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalInputReport;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * G4IT Inventory evaluation report.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "g4it_evaluation_report")
public class InventoryEvaluationReport implements Serializable {

    /**
     * Unique identifier.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Linked inventory.
     */
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id")
    private Inventory inventory;

    /**
     * Batch status code.
     */
    private String batchStatusCode;

    /**
     * Batch name, the unique numecoeval identifier.
     */
    private String batchName;

    /**
     * Batch progress in percentage.
     */
    private String progressPercentage;

    /**
     * Batch creation time.
     */
    private LocalDateTime createTime;

    /**
     * Batch end time.
     */
    private LocalDateTime endTime;

    /**
     * Criteria list to evaluate impacts on
     */
    private List<String> criteria;

    /**
     * NumEcoEval inputs integration reports.
     */
    @OneToMany(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "batchName", referencedColumnName = "batchName", insertable = false, updatable = false)
    private List<NumEcoEvalInputReport> numEcoEvalInputReports;

    /**
     * NumEcoEval calculation report.
     */
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "batchName", referencedColumnName = "batchName", insertable = false, updatable = false)
    private NumEcoEvalCalculationReport numEcoEvalCalculationReport;

    /**
     * Boolean isApplicationAggregated, used for data migration
     */
    private Boolean isApplicationAggregated;
}
