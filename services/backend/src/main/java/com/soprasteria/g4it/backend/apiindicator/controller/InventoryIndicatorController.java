/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
package com.soprasteria.g4it.backend.apiindicator.controller;

import com.soprasteria.g4it.backend.apibatchexport.business.InventoryExportService;
import com.soprasteria.g4it.backend.apiindicator.business.InventoryIndicatorService;
import com.soprasteria.g4it.backend.apiindicator.mapper.IndicatorRestMapper;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationIndicatorBO;
import com.soprasteria.g4it.backend.apiindicator.model.ApplicationVmImpactBO;
import com.soprasteria.g4it.backend.apiindicator.model.EquipmentIndicatorBO;
import com.soprasteria.g4it.backend.apiuser.business.UserService;
import com.soprasteria.g4it.backend.server.gen.api.InventoryIndicatorApiDelegate;
import com.soprasteria.g4it.backend.server.gen.api.dto.*;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Inventory Indicator Job Service.
 */
@Service
@NoArgsConstructor
public class InventoryIndicatorController implements InventoryIndicatorApiDelegate {

    @Autowired
    private InventoryIndicatorService inventoryIndicatorService;

    @Autowired
    private InventoryExportService inventoryExportService;

    @Autowired
    private IndicatorRestMapper indicatorRestMapper;


    /**
     * Service to access user data.
     */
    @Autowired
    private UserService userService;

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Map<String, EquipmentIndicatorRest>> getEquipmentIndicators(final String subscriber,
                                                                                      final String organization,
                                                                                      final Long inventoryId) {
        final Map<String, EquipmentIndicatorBO> indicators = inventoryIndicatorService.getEquipmentIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.ok().body(indicators.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, map -> this.indicatorRestMapper.toDto(map.getValue()))));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ApplicationIndicatorRest>> getApplicationIndicators(final String subscriber,
                                                                                   final String organization,
                                                                                   final Long inventoryId) {
        final List<ApplicationIndicatorBO<ApplicationImpactBO>> indicators = inventoryIndicatorService.getApplicationIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.ok().body(this.indicatorRestMapper.toApplicationIndicatorDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<ApplicationVmIndicatorRest>> getApplicationVmIndicators(final String subscriber,
                                                                                       final String organization,
                                                                                       final Long inventoryId,
                                                                                       final String applicationName,
                                                                                       final String criteria) {
        final List<ApplicationIndicatorBO<ApplicationVmImpactBO>> indicators = inventoryIndicatorService
                .getApplicationVmIndicators(subscriber, organization, inventoryId, applicationName, criteria);
        return ResponseEntity.ok().body(this.indicatorRestMapper.toApplicationVmIndicatorDto(indicators));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentLowCarbonRest>> getPhysicalEquipmentLowCarbon(final String subscriber,
                                                                                              final String organization,
                                                                                              final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toLowCarbonDto(inventoryIndicatorService.getPhysicalEquipmentLowCarbon(subscriber, organization, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<PhysicalEquipmentsAvgAgeRest>> getPhysicalEquipmentAvgAge(final String subscriber,
                                                                                         final String organization,
                                                                                         final Long inventoryId) {
        return ResponseEntity.ok(indicatorRestMapper.toAvgAgeDto(inventoryIndicatorService.getPhysicalEquipmentAvgAge(subscriber, organization, inventoryId)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> deleteIndicators(final String subscriber,
                                                 final String organization,
                                                 final Long inventoryId) {
        inventoryIndicatorService.deleteIndicators(subscriber, organization, inventoryId);
        return ResponseEntity.noContent().build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<List<DataCentersInformationRest>> getDataCenterIndicators(final String subscriber,
                                                                                    final String organization,
                                                                                    final Long inventoryId) {
        return ResponseEntity.ok().body(
                indicatorRestMapper.toDataCenterDto(inventoryIndicatorService.getDataCenterIndicators(subscriber, organization, inventoryId))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<EquipmentFiltersRest> getEquipmentFilters(final String subscriber,
                                                                    final String organization,
                                                                    final Long inventoryId) {
        return ResponseEntity.ok().body(
                indicatorRestMapper.toEquipmentFiltersDto(inventoryIndicatorService.getEquipmentFilters(subscriber, organization, inventoryId))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<ApplicationFiltersRest> getApplicationFilters(final String subscriber,
                                                                        final String organization,
                                                                        final Long inventoryId,
                                                                        final String domain,
                                                                        final String subDomain,
                                                                        final String applicationName) {
        return ResponseEntity.ok().body(
                indicatorRestMapper.toApplicationFiltersDto(inventoryIndicatorService.getApplicationFilters(subscriber, organization, inventoryId, domain, subDomain, applicationName))
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseEntity<Void> exportIndicators(final String subscriber,
                                                 final String organization,
                                                 final Long inventoryId) {
        inventoryExportService.createExportRequest(subscriber, organization, inventoryId, userService.getUser().getUsername());
        return ResponseEntity.<Void>ok().build();
    }
}
