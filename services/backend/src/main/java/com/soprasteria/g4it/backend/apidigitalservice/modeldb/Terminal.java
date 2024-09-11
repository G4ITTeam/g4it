/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.DeviceTypeRef;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Terminal Entity.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "terminal")
public class Terminal extends AbstractBaseEntity {

    /**
     * Primary Key : UID.
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
     * Device Type.
     */
    @OneToOne
    @JoinColumn(name = "device_type", referencedColumnName = "id")
    private DeviceTypeRef deviceType;

    /**
     * Country.
     */
    private String country;

    /**
     * Users count.
     */
    private Integer numberOfUsers;

    /**
     * Yearly Usage.
     */
    private Double yearlyUsageTimePerUser;

    /**
     * Device's lifespan.
     */
    private Double lifespan;

    /**
     * Used by csv-headers.yml
     *
     * @return the deviceType description
     */
    public String getDeviceTypeDescription() {
        return deviceType.getDescription();
    }
}
