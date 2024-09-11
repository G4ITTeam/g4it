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
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Virtual Equipment for Digital Service.
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "virtual_equipment_digital_service")
public class VirtualEquipmentDigitalService extends AbstractBaseEntity {

    /**
     * Primary Key : uid.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String uid;

    /**
     * Name.
     */
    @NotNull
    private String name;

    /**
     * Type.
     */
    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "virtual_equipment_characteristic_id", referencedColumnName = "id")
    private VirtualEquipmentCharacteristic virtualEquipmentCharacteristic;

    /**
     * Linked Server.
     */
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_uid", referencedColumnName = "uid")
    private Server server;

    /**
     * Quantity.
     */
    @NotNull
    private Integer quantity;

    /**
     * Annual usage time.
     */
    @NotNull
    private Integer annualUsageTime;

    /**
     * Used by csv-headers.yml
     *
     * @return the deviceType description
     */
    public String getVirtualEquipmentCharacteristicType() {
        return virtualEquipmentCharacteristic.getType();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the virtualEquipmentCharacteristic characteristicValue
     */
    public Integer getVirtualEquipmentCharacteristicCharacteristicValue() {
        return virtualEquipmentCharacteristic.getCharacteristicValue();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the server name
     */
    public String getServerName() {
        return server.getName();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the server annualOperatingTime
     */
    public Integer getServerAnnualOperatingTime() {
        return server.getAnnualOperatingTime();
    }

}
