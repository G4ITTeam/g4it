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
@Table(name = "data_center")
public class DataCenter extends AbstractValidationBaseEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{nomcourtdatacenter.not.blank}")
    @Size(max = 255, message = "{nomcourtdatacenter.size}")
    private String nomCourtDatacenter;

    @Size(max = 255, message = "{nomlongdatacenter.size}")
    private String nomLongDatacenter;

    @Pattern(regexp = "|^1[.]\\d[1-9]$|^1[.][1-9]\\d{0,1}$|^1\\d{1}([.]\\d{0,2})?$|^[2-9]\\d{0,1}([.]\\d{0,2})?$", message = "{pue.pattern}")
    @Size(max = 255, message = "{pue.size}")
    private String pue;

    @NotBlank(message = "{localisation.not.blank}")
    @Size(max = 255, message = "{localisation.size}")
    private String localisation;

    @Size(max = 255, message = "{nomentite.size}")
    private String nomEntite;

    @Size(max = 255, message = "{nomsourcedonnee.size}")
    private String nomSourceDonnee;

}
