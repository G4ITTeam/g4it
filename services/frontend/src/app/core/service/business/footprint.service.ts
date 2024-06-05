/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Observable, forkJoin, lastValueFrom, map, of, tap } from "rxjs";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import {
    Filter,
    FilterApplication,
    FilterApplicationReceived,
    FilterRepository,
} from "src/app/core/store/filter.repository";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
    FootprintRepository,
} from "src/app/core/store/footprint.repository";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";

@Injectable({
    providedIn: "root",
})
export class FootprintService {
    constructor(
        private footprintDataService: FootprintDataService,
        private inventoryDataService: InventoryDataService,
        private filterRepo: FilterRepository,
        private footprintRepo: FootprintRepository,
        private echartsRepo: EchartsRepository,
        private inventoryRepo: InventoryRepository,
        private translate: TranslateService
    ) {}

    async retrieveFootprint(
        selectedInventoryId: number,
        selectedCriteria: string,
        selectedView: string
    ): Promise<void> {
        const inventories = await lastValueFrom(
            this.inventoryDataService.getInventories(selectedInventoryId)
        );
        let inventory;
        if (inventories.length > 1) {
            inventory = inventories.map(
                (inventory) => inventory.id === selectedInventoryId
            );
        } else {
            inventory = inventories;
        }

        // Early exit if we are on an unknown inventory
        if (!inventory) {
            return;
        }
        // Otherwise, we proceed with fetching data from API
        this.inventoryRepo.updateSelectedInventory(selectedInventoryId);
        if (selectedView === "equipment") {
            // Fetch all data from api and initialize stores
            forkJoin([
                this.initFilters(selectedInventoryId),
                this.initFootprint(selectedInventoryId),
                this.initDatacenters(selectedInventoryId),
                this.initPhysicalEquipments(selectedInventoryId),
            ]).subscribe(([filters]) => {
                // Refresh active impacts
                this.refreshActiveImpacts(filters);
                // Init selected criteria from url
                this.updateSelectedCriteria(selectedCriteria);
                // We can signal that all data has been initialized
                this.echartsRepo.setIsDataInitialized(true);
            });
        } else if (selectedView === "application") {
            // Fetch all data from api and initialize stores
            const actualAppFootprint = this.footprintRepo.getValueApplicationFootprint();

            let currentInventoryId = null;
            for (const key in actualAppFootprint) {
                currentInventoryId = actualAppFootprint[key].id || 'undefined';
                break;
            }

            if (currentInventoryId === selectedInventoryId) {
                this.initFiltersApplication(selectedInventoryId).subscribe(() => {
                    this.echartsRepo.setIsDataInitialized(true);
                });
            } else {
                forkJoin([
                    this.initFiltersApplication(selectedInventoryId),
                    this.initApplicationFootprint(selectedInventoryId),
                ]).subscribe(() => {
                    this.echartsRepo.setIsDataInitialized(true);
                });
            }
        }
    }

    initFootprint(inventoryId: number) {
        return this.footprintDataService
            .getFootprint(inventoryId)
            .pipe(tap(this.footprintRepo.initStores));
    }

    initFilters(inventoryId: number): Observable<Filter> {
        return this.footprintDataService
            .getFilters(inventoryId)
            .pipe(
                map(this.cleanFilters),
                tap(this.filterRepo.setAllFilters),
                tap(this.filterRepo.updateSelectedFilters)
            );
    }

    initFiltersApplication(inventoryId: number): Observable<FilterApplication> {
        const appGraph = this.footprintRepo.getValueAppGraphPositionStore();

        return this.footprintDataService
            .getApplicationFilters(
                inventoryId,
                appGraph.domain,
                appGraph.subdomain,
                appGraph.app
            )
            .pipe(
                map(this.cleanApplicationFilters),
                tap(this.filterRepo.setAllFiltersApp),
                tap(this.filterRepo.updateSelectedFiltersApp)
            );
    }

    initDatacenters(inventoryId: number) {
        return this.footprintDataService
            .getDatacenters(inventoryId)
            .pipe(tap(this.footprintRepo.setDatacenters));
    }

    initPhysicalEquipments(inventoryId: number) {
        return this.footprintDataService
            .getPhysicalEquipments(inventoryId)
            .pipe(
                tap(([averageAges, lowImpact]) =>
                    this.footprintRepo.setPhysicalEquipmentStats(averageAges, lowImpact)
                )
            );
    }

    sendExportRequest(inventoryId: number): Observable<number> {
        return this.footprintDataService.sendExportRequest(inventoryId);
    }

    deleteIndicators(inventoryId: number) {
        return this.footprintDataService.deleteIndicators(inventoryId);
    }

    updateSelectedCriteria(criteria: string, unite: string = "") {
        this.filterRepo.updateSelectedCriteria(criteria);
        if (unite === "") {
            this.echartsRepo.setUnitOfCriteria(this.getUnitFromCriteria(criteria));
        } else {
            this.echartsRepo.setUnitOfCriteria(unite);
        }
    }

    initApplicationFootprint(inventoryId: number) {
        return this.footprintDataService.getApplicationFootprint(inventoryId).pipe(
            tap((footprint) => {
                footprint = this.setUnspecifiedData(footprint);
                footprint.forEach((indicateur) => {
                    indicateur.criteriaTitle = this.translate.instant(
                        `criteria.${indicateur.criteria}.title`
                    );
                    indicateur.id = inventoryId;
                });
                this.footprintRepo.setApplicationFootprint(footprint);
            })
        );
    }

    initApplicationCriteriaFootprint(
        inventoryId: number,
        app: string,
        criteria: string
    ) {
        return this.mapApplicationCriteria(inventoryId, app, criteria);
    }

    mapApplicationCriteria(inventoryId: number, app: string, criteria: string) {
        return this.footprintDataService
            .getApplicationCriteriaFootprint(inventoryId, app, criteria)
            .pipe(
                tap((footprint) => {
                    footprint = this.setUnspecifiedDataApp(footprint);
                    footprint.forEach((indicateur) => {
                        indicateur.criteriaTitle = this.translate.instant(
                            `criteria.${indicateur.criteria}.title`
                        );
                    });
                    this.footprintRepo.setApplicationCriteriaFootprint(footprint);
                })
            );
    }

    setUnspecifiedDataApp(footprint: ApplicationCriteriaFootprint[]) {
        const excludeFields = ["sip", "impact"];
        footprint.forEach((element) => {
            element.impacts.forEach((impact: any) => {
                for (const key in impact) {
                    if (impact[key] === "") {
                        impact[key] = Constants.UNSPECIFIED;
                    }
                }
            });
        });
        return footprint;
    }

    setUnspecifiedData(footprint: ApplicationFootprint[]) {
        const excludeFields = ["sip", "cluster"];

        footprint.forEach((element) => {
            element.impacts.forEach((impact: any) => {
                for (const key in impact) {
                    if (impact[key] === "") {
                        impact[key] = Constants.UNSPECIFIED;
                    }
                }
            });
        });
        return footprint;
    }

    refreshActiveImpacts(filters: Filter): void {
        this.footprintRepo.updateActiveImpacts(filters);
    }

    private getUnitFromCriteria(criteria: string): string {
        return this.translate.instant(`criteria.${criteria}.unite`);
    }

    private cleanFilters(rawFilters: Filter) {
        // Replace null values by "Empty" and add "All" value
        return Object.keys(rawFilters).reduce((acc, key) => {
            acc[key as keyof Filter] = [
                "All",
                ...rawFilters[key as keyof Filter].map((item) => item || "Empty").sort(),
            ];
            return acc;
        }, {} as Filter);
    }

    private cleanApplicationFilters(
        rawFilters: FilterApplicationReceived
    ): FilterApplication {
        const transformedFilters: FilterApplication = {
            environments: [...rawFilters.environments],
            types: [...rawFilters.types],
            lifeCycles: [],
            domains: [],
        };

        const lifecyleMap = LifeCycleUtils.getLifeCycleMap();

        transformedFilters.lifeCycles = rawFilters.lifeCycles.map(
            (lifeCycle) => lifecyleMap.get(lifeCycle) || lifeCycle
        );

        transformedFilters.domains = rawFilters.domains.map((domain) => {
            if (domain.name === "") {
                domain.name = Constants.UNSPECIFIED;
            }

            let domainString = domain.name;
            const subDomains = domain.subDomains
                .map((subdomain) => (subdomain === "" ? Constants.UNSPECIFIED : subdomain))
                .join(",");

            if (subDomains.length > 0) {
                domainString += "," + subDomains;
            }

            return domainString;
        });

        // Replace null values by "Empty" and add "All" value
        return Object.keys(transformedFilters).reduce((acc, key) => {
            acc[key as keyof FilterApplicationReceived] = [
                "All",
                ...transformedFilters[key as keyof FilterApplicationReceived]
                    .map((item: any) => item || Constants.UNSPECIFIED)
                    .sort(),
            ];
            return acc;
        }, {} as FilterApplication);
    }
}
