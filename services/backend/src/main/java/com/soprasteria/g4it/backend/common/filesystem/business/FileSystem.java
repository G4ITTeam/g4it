/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.common.filesystem.business;

/**
 * FileSystem interface.
 */
public interface FileSystem {

    /**
     * Mount file storage for given subscriber.
     *
     * @param subscriber   the client subscriber.
     * @param organization the subscriber organization.
     * @return a FileStorage for this subscriber.
     */
    FileStorage mount(final String subscriber, final String organization);

}
