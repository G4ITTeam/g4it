/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.InPhysicalEquipmentElecConsumptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository to calculate the electric consumption of physical equipment indicators view.
 */
@Repository
public interface InPhysicalEquipmentElecConsumptionViewRepository extends JpaRepository<InPhysicalEquipmentElecConsumptionView, Long> {

    /**
     * method to recovery of physical equipment electricity consumption indicators.
     *
     * @param taskId         the taskId
     * @param criteriaNumber the number of criteria
     * @return main indicators
     */
    @Query(nativeQuery = true)
    List<InPhysicalEquipmentElecConsumptionView> findPhysicalEquipmentElecConsumptionIndicators(@Param("taskId") final Long taskId,
                                                                                                @Param("criteriaNumber") final Long criteriaNumber);

}
