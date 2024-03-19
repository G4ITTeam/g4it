/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { Subject, combineLatestWith, takeUntil } from "rxjs";
import { FilterRepository } from "src/app/core/store/filter.repository";
import { Datacenter, FootprintRepository } from "src/app/core/store/footprint.repository";

@Component({
    selector: "app-datacenter-stats",
    templateUrl: "./datacenter-stats.component.html",
})
export class DatacenterStatsComponent implements OnInit {
    inventoryDate = "";
    datacenters: Datacenter[] = [];
    selectedCountries: string[] = [];
    selectedEntities: string[] = [];
    selectedEquipments: string[] = [];
    selectedStatus: string[] = [];

    datacenterAvgWeightedPue: number = 0;
    datacenterCount: number = 0;

    constructor(
        private filterRepo: FilterRepository,
        public footprintRepo: FootprintRepository
    ) {}

    ngOnInit(): void {
        this.footprintRepo.datacentersStats$
            .pipe(
                combineLatestWith(this.filterRepo.selectedFilters$),
                takeUntil(this.ngUnsubscribe)
            )
            .subscribe(([datacenters, filters]) => {
                const { countries, entities, status, equipments } = filters;
                this.computeDataCenterStats(
                    datacenters,
                    countries,
                    entities,
                    equipments,
                    status
                );
            });
    }

    private ngUnsubscribe = new Subject<void>();

    computeDataCenterStats(
        datacenters: Datacenter[],
        selectedCountries: string[],
        selectedEntities: string[],
        selectedEquipments: string[],
        selectedStatus: string[]
    ) {
        datacenters = datacenters || [];
        let datacenterPhysicalEquipmentCount = 0;
        let datacenterSum = 0;
        let datacenterCount = 0;
        const datacenterNameList: string[] = [];
        datacenters.forEach((datacenter) => {
            let {
                country,
                entity,
                equipment,
                status,
                dataCenterName,
                physicalEquipmentCount,
                pue,
            } = datacenter;
            country = country || "Empty";
            entity = entity || "Empty";
            equipment = equipment || "Empty";
            status = status || "Empty";
            pue = pue || 0;
            dataCenterName = dataCenterName || "";
            physicalEquipmentCount = physicalEquipmentCount || 0;
            if (
                selectedCountries.indexOf(country) !== -1 &&
                selectedEntities.indexOf(entity) !== -1 &&
                selectedEquipments.indexOf(equipment) !== -1 &&
                selectedStatus.indexOf(status) !== -1
            ) {
                datacenterPhysicalEquipmentCount += physicalEquipmentCount;
                datacenterSum += physicalEquipmentCount * pue;

                if (datacenterNameList.indexOf(dataCenterName) == -1) {
                    datacenterCount += 1;
                    datacenterNameList.push(dataCenterName);
                }
            }
        });
        if (datacenterPhysicalEquipmentCount === 0) {
            // No physical equipment for selected filter
            this.datacenterAvgWeightedPue = 0;
        } else {
            this.datacenterAvgWeightedPue =
                datacenterSum / datacenterPhysicalEquipmentCount;
        }
        this.datacenterCount = datacenterCount;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
