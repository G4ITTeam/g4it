/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apibatchexport.modeldb;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "g4it_export_report")
public class ExportReport extends AbstractBaseEntity implements Serializable {

    public static final String REQUESTED = "REQUESTED";

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * Identifier of the inventory related to the export request
     */
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventoryId", insertable = false, updatable = false)
    private Inventory inventory;

    /**
     * The inventory ID.
     */
    private Long inventoryId;

    /**
     * Status of the export request
     */
    private String statusCode;

    /**
     * Indicators batch name related to the export request
     */
    private String batchName;

    /**
     * Batch creation time.
     */
    private LocalDateTime batchCreateTime;

    /**
     * Batch end time.
     */
    private LocalDateTime batchEndTime;

    /**
     * Sending export time.
     */
    private LocalDateTime sendingExportTime;

    /**
     * The username who made the export request.
     */
    private String username;

    /**
     * The url of the zip file.
     */
    private String exportFilename;

    /**
     * The size of the export file.
     */
    private Long exportFileSize;

}
