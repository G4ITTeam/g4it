/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.PhysicalEquipmentAvgAgeView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.*;

/**
 * Repository to calculate the physical equipment avg age indicators view.
 */
@Repository
public interface PhysicalEquipmentAvgAgeViewRepository extends JpaRepository<PhysicalEquipmentAvgAgeView, Long> {

    /**
     * method to recovery of physical equipment avg age indicators.
     *
     * @param subscriber   the subscriber.
     * @param organization the organization name.
     * @param inventoryId  the inventory id.
     * @return main indicators
     */
    @Query(nativeQuery = true)
    List<PhysicalEquipmentAvgAgeView> findPhysicalEquipmentAvgAgeIndicators(@Param(PARAM_SUBSCRIBER) final String subscriber,
                                                                            @Param(PARAM_ORGANIZATION) final String organization,
                                                                            @Param(PARAM_INVENTORY_ID) final Long inventoryId);
}
