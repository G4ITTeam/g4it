/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apidigitalservice.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRef;
import com.soprasteria.g4it.backend.apidigitalservice.modeldb.referential.ServerHostRefDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_TYPE;

/**
 * Device Type Referential repository.
 */
@Repository
public interface ServerHostRefRepository extends JpaRepository<ServerHostRef, Long> {

    /**
     * Find by type.
     *
     * @param type (Compute or Storage).
     * @return server host referential list.
     */
    @Query(nativeQuery = true)
    List<ServerHostRefDTO> findServerHostRefByType(@Param(PARAM_TYPE) final String type);

    /**
     * Find by identifier.
     *
     * @param id the server host ref unique id.
     * @return the server host.
     */
    Optional<ServerHostRef> findById(final long id);
}
