/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Server Entity
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "server")
public class Server extends AbstractBaseEntity {

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
     * Server characteristic.
     */
    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true)
    @JoinColumn(name = "server_characteristic_id", referencedColumnName = "id")
    private ServerCharacteristic serverCharacteristic;

    /**
     * Linked Datacenter.
     * Not all cascade to not remove datacenter when server is remove.
     */
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "datacenter_uid", referencedColumnName = "uid")
    private DatacenterDigitalService datacenterDigitalService;

    /**
     * Linked virtual equipments.
     */
    @Builder.Default
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY, mappedBy = "server")
    private List<VirtualEquipmentDigitalService> virtualEquipmentDigitalServices = new ArrayList<>();

    /**
     * Server host.
     */
    @OneToOne
    @JoinColumn(name = "server_host_id", referencedColumnName = "id")
    private ServerHostRef serverHost;

    /**
     * The server name.
     */
    private String name;

    /**
     * Mutualization Type : Dedicated / Shared.
     */
    private String mutualizationType;

    /**
     * Type : Compute / Storage
     */
    private String type;

    /**
     * Quantity.
     */
    private Integer quantity;

    /**
     * Lifespan.
     */
    private Double lifespan;

    /**
     * Electricity consumption.
     */
    private Integer annualElectricityConsumption;

    /**
     * Annual usage time.
     */
    private Integer annualOperatingTime;

    /**
     * Add virtual equipment.
     *
     * @param virtualEquipmentDigitalService virtual equipment to add.
     */
    public void addVirtualEquipment(final VirtualEquipmentDigitalService virtualEquipmentDigitalService) {
        virtualEquipmentDigitalService.setServer(this);
        virtualEquipmentDigitalServices.add(virtualEquipmentDigitalService);
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the datacenterDigitalService name
     */
    public String getDatacenterDigitalServiceName() {
        return datacenterDigitalService.getName();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the serverCharacteristic type
     */
    public String getServerCharacteristicType() {
        return serverCharacteristic.getType();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the serverHost description
     */
    public String getServerHostDescription() {
        return serverHost.getDescription();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the serverHost externalReferentialDescription
     */
    public String getServerHostExternalReferentialDescription() {
        return serverHost.getExternalReferentialDescription();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the serverHost nbOfVcpu
     */
    public Integer getServerHostNbOfVcpu() {
        return serverCharacteristic.getCharacteristicValue();
    }

    /**
     * Used by csv-headers.yml
     *
     * @return the serverHost totalDisk
     */
    public Integer getServerHostTotalDisk() {
        return serverCharacteristic.getCharacteristicValue();
    }

}
