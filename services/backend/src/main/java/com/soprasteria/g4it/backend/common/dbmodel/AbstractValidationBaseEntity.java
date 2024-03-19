/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.dbmodel;

import com.soprasteria.g4it.backend.apiinventory.modeldb.Inventory;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.batch.item.ResourceAware;
import org.springframework.core.io.Resource;

import java.util.Date;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@ToString(callSuper = true)
public abstract class AbstractValidationBaseEntity extends AbstractBaseEntity implements ResourceAware {

    /**
     * Entity state (flag to indicate if valid or not).
     */
    @Transient
    private Boolean valid;

    /**
     * Possible error message.
     */
    @Transient
    private String message;

    /**
     * Input source filename
     */
    @Size(max = 255, message = "{filename.size}")
    private String inputFileName;

    /**
     * Processing date.
     */
    private Date sessionDate;

    /**
     * Line Number.
     */
    private Integer lineNumber;

    /**
     * Input resource.
     */
    @ToString.Exclude
    @Transient
    private Resource resource;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventoryId", insertable = false, updatable = false)
    private Inventory inventory;

    private long inventoryId;
}
