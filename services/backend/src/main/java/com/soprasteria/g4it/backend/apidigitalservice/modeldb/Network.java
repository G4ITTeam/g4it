/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apidigitalservice.modeldb;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.NetworkTypeRef;
import com.soprasteria.g4it.backend.common.dbmodel.AbstractBaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Network Entity
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@SuperBuilder
@Entity
@Table(name = "network")
public class Network extends AbstractBaseEntity {

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
     * Network Type.
     */
    @OneToOne
    @JoinColumn(name = "network_type", referencedColumnName = "id")
    private NetworkTypeRef networkType;

    /**
     * Yearly quantity of GigaByte exchanged.
     */
    private Double yearlyQuantityOfGbExchanged;

    /**
     * Used by csv-headers.yml
     *
     * @return the networkType type
     */
    public String getNetworkTypeDescription() {
        return networkType.getDescription();
    }


}
