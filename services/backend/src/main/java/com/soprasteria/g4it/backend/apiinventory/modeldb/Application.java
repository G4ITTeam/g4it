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
@Table(name = "application")
public class Application extends AbstractValidationBaseEntity implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{nomapplication.not.blank}")
    @Size(max = 255, message = "{nomapplication.size}")
    private String nomApplication;

    @NotBlank(message = "{typeenvironnement.not.blank}")
    @Size(max = 255, message = "{typeenvironnement.size}")
    private String typeEnvironnement;

    @NotBlank(message = "{nomequipementvirtuel.not.blank}")
    @Size(max = 255, message = "{nomequipementvirtuel.size}")
    @Column(name = "nom_vm")
    private String nomEquipementVirtuel;

    @Size(max = 255, message = "{domaine.size}")
    private String domaine;

    @Size(max = 255, message = "{sousdomaine.size}")
    private String sousDomaine;

    @Size(max = 255, message = "{nomentite.size}")
    private String nomEntite;

    @Size(max = 255, message = "{nomequipementphysique.size}")
    private String nomEquipementPhysique;

    @Size(max = 255, message = "{nomsourcedonnee.size}")
    private String nomSourceDonnee;

    @Size(max = 255, message = "{nomsourcedonneeequipementvirtuel.size}")
    private String nomSourceDonneeEquipementVirtuel;

}
