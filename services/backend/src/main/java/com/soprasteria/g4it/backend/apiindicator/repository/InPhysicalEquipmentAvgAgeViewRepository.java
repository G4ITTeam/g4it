/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentAvgAgeView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to calculate the physical equipment avg age indicators view.
 */
@Repository
public interface InPhysicalEquipmentAvgAgeViewRepository extends JpaRepository<InPhysicalEquipmentAvgAgeView, Long> {

    /**
     * method to recovery of physical equipment avg age indicators.
     *
     * @param taskId the taskId
     * @return main indicators
     */
    @Query(nativeQuery = true)
    List<InPhysicalEquipmentAvgAgeView> findPhysicalEquipmentAvgAgeIndicators(@Param("taskId") final Long taskId);
}
