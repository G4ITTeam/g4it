/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apidigitalservice.modeldb.DigitalServiceTerminalIndicatorView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_UID;

@Repository
public interface DigitalServiceTerminalIndicatorRepository extends JpaRepository<DigitalServiceTerminalIndicatorView, String> {

    List<DigitalServiceTerminalIndicatorView> findDigitalServiceTerminalIndicators(@Param(PARAM_UID) final String uid);
}
