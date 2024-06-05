/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiuser.repository;

import com.soprasteria.g4it.backend.apiuser.modeldb.Role;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * Find Role by name.
     *
     * @param name the role name.
     * @return the role.
     */
    @Cacheable("RoleRepository_findByName")
    Role findByName(final String name);
}
