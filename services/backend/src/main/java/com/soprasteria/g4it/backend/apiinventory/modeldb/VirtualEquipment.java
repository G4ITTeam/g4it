/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiinventory.modeldb;

import com.soprasteria.g4it.backend.common.dbmodel.AbstractValidationBaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
@NoArgsConstructor
@Entity
@SuperBuilder
@Table(name = "equipement_virtuel")
public class VirtualEquipment extends AbstractValidationBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_vm")
    @NotBlank(message = "{nomequipementvirtuel.not.blank}")
    @Size(max = 255, message = "{nomequipementvirtuel.size}")
    private String nomEquipementVirtuel;

    @Column(name = "v_cpu")
    @Pattern(regexp = "^\\+?\\d+\\.?\\d*$|", message = "{vcpu.pattern}")
    private String vCPU;

    @Size(max = 255, message = "{nomentite.size}")
    private String nomEntite;

    @Size(max = 255, message = "{cluster.size}")
    private String cluster;

    @Size(max = 255, message = "{nomequipementphysique.size}")
    @NotBlank(message = "{nomequipementphysique.not.blank}")
    private String nomEquipementPhysique;

    @Size(max = 255, message = "{nomsourcedonneeequipementphysique.size}")
    private String nomSourceDonneeEquipementPhysique;

    @Size(max = 255, message = "{nomsourcedonnee.size}")
    private String nomSourceDonnee;

    @Size(max = 255, message = "{consoelecan.size}")
    @Pattern(regexp = "^[1-9][\\d]*$|", message = "{consoelecan.pattern}")
    private String consoElecAn;

    @Size(max = 255, message = "{typeeqv.size}")
    @NotBlank(message = "{typeeqv.not.blank}")
    private String typeEqv;

    @Size(max = 255, message = "{clerepartition.size}")
    @Pattern(regexp = "^\\+?\\d+\\.?\\d*$|", message = "{clerepartition.pattern}")
    private String cleRepartition;

    @ToString.Exclude
    @OneToOne(targetEntity = PhysicalEquipment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "inventoryId", referencedColumnName = "inventoryId", insertable = false, updatable = false)
    @JoinColumn(name = "nomEquipementPhysique", referencedColumnName = "nomEquipementPhysique", insertable = false, updatable = false)
    private PhysicalEquipment physicalEquipment;

    @Size(max = 255, message = "{capacitestockage.size}")
    @Pattern(regexp = "[+-]?(\\d*[.])?\\d+|", message = "{capacitestockage.pattern}")
    private String capaciteStockage;

}
