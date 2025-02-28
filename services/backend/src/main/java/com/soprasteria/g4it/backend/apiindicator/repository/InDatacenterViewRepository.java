/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
package com.soprasteria.g4it.backend.apiindicator.repository;

import com.soprasteria.g4it.backend.apiindicator.modeldb.InDatacenterIndicatorView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.soprasteria.g4it.backend.apiindicator.utils.Constants.PARAM_INVENTORY_ID;

/**
 * Repository to calculate the datacenter indicators view.
 */
@Repository
public interface InDatacenterViewRepository extends JpaRepository<InDatacenterIndicatorView, Long> {

    /**
     * Recovery of datacenter indicators.
     *
     * @param inventoryId the inventory id.
     * @return main indicators
     */
    @Query(nativeQuery = true)
    List<InDatacenterIndicatorView> findDataCenterIndicators(@Param(PARAM_INVENTORY_ID) final Long inventoryId);


}
