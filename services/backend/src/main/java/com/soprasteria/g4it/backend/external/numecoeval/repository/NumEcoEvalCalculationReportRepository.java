/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.external.numecoeval.repository;

import com.soprasteria.g4it.backend.external.numecoeval.modeldb.NumEcoEvalCalculationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * NumEcoEval Calculation Report Repository.
 */
@Repository
public interface NumEcoEvalCalculationReportRepository extends JpaRepository<NumEcoEvalCalculationReport, Long> {

}
