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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

/**
 * Num Eco Eval Calculation report.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@Table(name = "numecoeval_calculation_report")
public class NumEcoEvalCalculationReport extends AbstractBaseEntity implements Serializable {

    /**
     * Auto Generated Id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Attached Batch Name.
     */
    private String batchName;

    /**
     * Datacenter number.
     */
    private Integer datacenterNumber;

    /**
     * PhysicalEquipment number.
     */
    private Integer physicalEquipmentNumber;

    /**
     * VirtualEquipment number.
     */
    private Integer virtualEquipmentNumber;

    /**
     * Application number.
     */
    private Integer applicationNumber;

    /**
     * Messaging number.
     */
    private Integer messagingNumber;

}
