/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Network type reference.
 */
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "ref_network_type")
public class NetworkTypeRef {

    /**
     * To prevent update.
     */
    @PreUpdate
    private void preUpdate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Auto generated ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Descrption.
     */
    private String description;

    /**
     * Reference.
     */
    private String reference;

    /**
     * External reference description.
     */
    private String externalReferentialDescription;

    /**
     * Network type.
     */
    private String type;

    /**
     * Annual quantity of GigaOctet
     */
    private Integer annualQuantityOfGo;

    /**
     * Country.
     */
    private String country;

    /**
     * Source.
     */
    private String source;

}
