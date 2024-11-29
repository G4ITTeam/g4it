/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, computed, inject } from "@angular/core";

import {
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
    Stat,
} from "src/app/core/interfaces/footprint.interface";
import { InventoryUtilService } from "src/app/core/service/business/inventory-util.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-datacenter-equipment-stats",
    templateUrl: "./datacenter-equipment-stats.component.html",
})
export class DataCenterEquipmentStatsComponent extends AbstractDashboard {
    private store = inject(FootprintStoreService);
    private inventoryUtilService = inject(InventoryUtilService);
    @Input() footprint: Criterias = {} as Criterias;
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() equipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];

    datacenterStats = computed<Stat[]>(() =>
        this.inventoryUtilService.computeDataCenterStats(
            this.store.filters(),
            this.filterFields,
            this.datacenters,
        ),
    );

    equipmentStats = computed<Stat[]>(() =>
        this.inventoryUtilService.computeEquipmentStats(
            this.equipments,
            this.store.filters(),
            this.filterFields,
            this.footprint,
        ),
    );
}
