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
 * Server Host Referential.
 */
@NamedNativeQuery(
        name = "ServerHostRef.findServerHostRefByType",
        resultSetMapping = "ServerHostRefDTO",
        query = """
                select distinct
                     ref_s.id                                                      as id,
                     ref_s.description                                             as description,
                     ref_s.type                                                    as type,
                     cast(ref_s.nb_of_vcpu as integer)                             as nbOfVcpu,
                     cast(ref_s.total_disk as integer)                             as totalDisk,
                     cast(ref_s.lifespan as numeric)                               as lifespan,
                     cast(ref_factcaract.conso_elec_moyenne as integer)            as annualElectricityConsumption
                from ref_server_host ref_s
                left join ref_facteurcaracterisation ref_factcaract on ref_s.reference = ref_factcaract.nom
                where ref_s.type = :type
                and ref_factcaract.niveau = '2-Equipement'
                """
)
@SqlResultSetMapping(
        name = "ServerHostRefDTO",
        classes = @ConstructorResult(
                targetClass = ServerHostRefDTO.class,
                columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "description", type = String.class),
                        @ColumnResult(name = "type", type = String.class),
                        @ColumnResult(name = "nbOfVcpu", type = Integer.class),
                        @ColumnResult(name = "totalDisk", type = Integer.class),
                        @ColumnResult(name = "lifespan", type = Double.class),
                        @ColumnResult(name = "annualElectricityConsumption", type = Integer.class)
                }
        )
)
@Data
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "ref_server_host")
public class ServerHostRef {

    /**
     * To prevent update.
     */
    @PreUpdate
    private void preUpdate() {
        throw new UnsupportedOperationException();
    }

    /**
     * Auto Generated ID.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Device's description.
     */
    private String description;

    /**
     * NumEcoEval Reference.
     */
    private String reference;

    /**
     * External description.
     */
    private String externalReferentialDescription;

    /**
     * Server host type.
     */
    private String type;

    /**
     * Number of VCpu.
     */
    @Column(name = "nb_of_vcpu")
    private Integer nbOfVcpu;

    /**
     * Total disk (in GB).
     */
    private Integer totalDisk;

    /**
     * Device's lifespan.
     */
    private Double lifespan;

}
