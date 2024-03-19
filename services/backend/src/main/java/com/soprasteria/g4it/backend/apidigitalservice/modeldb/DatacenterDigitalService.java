/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Datacenter for digital service.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "datacenter_digital_service")
public class DatacenterDigitalService extends AbstractBaseEntity {

    /**
     * Primary Key : uid.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    /**
     * Linked Digital Service.
     */
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "digital_service_uid")
    private DigitalService digitalService;

    /**
     * Server.
     */
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "datacenterDigitalService", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Server> server;

    /**
     * Name.
     */
    private String name;

    /**
     * Location.
     */
    private String location;

    /**
     * PUE.
     */
    private BigDecimal pue;
}
