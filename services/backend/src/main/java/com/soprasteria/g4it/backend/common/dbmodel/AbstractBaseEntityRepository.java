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

public interface AbstractBaseEntityRepository<T extends AbstractBaseEntity> {

    /**
     * Method to get entity by idInventaire.
     *
     * @param inventoryId inventory Id.
     * @param pageable    pagination
     * @return page of entity by idInventaire.
     */
    Page<T> findByInventoryId(final long inventoryId, final Pageable pageable);

    /**
     * Method to count entities by organisation and idInventaire
     *
     * @param inventoryId the inventory Id
     * @return number of entities associated
     */
    long countByInventoryId(long inventoryId);

}
