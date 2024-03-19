/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Server Business Object.
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
public class ServerBO {

    /**
     * Technical UID.
     */
    private String uid;

    /**
     * Creation date.
     */
    @EqualsAndHashCode.Exclude
    private LocalDateTime creationDate;

    /**
     * Server's name.
     */
    private String name;

    /**
     * Mutualization type (shared, dedicated).
     */
    private String mutualizationType;

    /**
     * Type (Compute, Storage).
     */
    private String type;

    /**
     * Hostname.
     */
    private ServerHostBO host;

    /**
     * Linked datacenter.
     */
    private ServerDataCenterBO datacenter;

    /**
     * Quantity.
     */
    private Integer quantity;

    /**
     * Total Disk (Go).
     */
    private Integer totalDisk;

    /**
     * Server's lifespan.
     */
    private Double lifespan;

    /**
     * Electric consumption.
     */
    private Integer annualElectricConsumption;

    /**
     * Annual Fixed time (Hours).
     */
    private Integer annualOperatingTime;

    /**
     * Total VCPU.
     */
    private Integer totalVCpu;

    /**
     * Virtual equipment.
     */
    private List<VirtualEquipmentBO> vm;

}
