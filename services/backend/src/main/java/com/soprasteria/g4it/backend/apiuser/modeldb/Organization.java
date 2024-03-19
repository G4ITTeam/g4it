/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiuser.modeldb;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

/**
 * G4IT Organization.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "g4it_organization")
public class Organization extends AbstractBaseEntity implements Serializable {

    /**
     * Auto generated id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    /**
     * The organization's name.
     */
    @NotNull
    private String name;

    /**
     * The storage retention day for export folder
     */
    private Integer storageRetentionDayExport;

    /**
     * The storage retention day for output folder
     */
    private Integer storageRetentionDayOutput;

    /**
     * The data retention day
     */
    private Integer dataRetentionDay;

    /**
     * The organization's subscriber.
     */
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "subscriber_id", referencedColumnName = "id")
    private Subscriber subscriber;

    /**
     * Identifier of the inventory related to the export request
     */
    @ToString.Exclude
    @OneToMany(mappedBy = "organization")
    private List<Inventory> inventory;

}
