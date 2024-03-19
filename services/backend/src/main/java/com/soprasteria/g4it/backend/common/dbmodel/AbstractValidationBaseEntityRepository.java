/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.common.dbmodel;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Date;

public interface AbstractValidationBaseEntityRepository<T extends AbstractValidationBaseEntity> extends AbstractBaseEntityRepository<T> {

    /**
     * Method to get entity by loading session date
     *
     * @param sessionDate session date.
     * @param pageable    pagination
     * @return list of entity by session date.
     */
    Page<T> findBySessionDate(final Date sessionDate, final Pageable pageable);

}
