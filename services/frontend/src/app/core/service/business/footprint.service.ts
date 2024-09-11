/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Observable, forkJoin, lastValueFrom, map, tap } from "rxjs";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InventoryDataService } from "src/app/core/service/data/inventory-data.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import {
    FilterApplication,
    FilterApplicationReceived,
    FilterRepository,
} from "src/app/core/store/filter.repository";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
    Criterias,
    FootprintRepository,
    Impact,
} from "src/app/core/store/footprint.repository";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { Filter } from "../../interfaces/filter.interface";
import { FootprintCalculated, SumImpact } from "../../interfaces/footprint.interface";

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
        private translate: TranslateService,
    ) {}

    async retrieveFootprint(
        selectedInventoryId: number,
        selectedCriteria: string,
        selectedView: string,
    ): Promise<void> {
        const inventories = await lastValueFrom(
            this.inventoryDataService.getInventories(selectedInventoryId),
        );
        let inventory;
        if (inventories.length > 1) {
            inventory = inventories.map(
                (inventory) => inventory.id === selectedInventoryId,
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
        if (selectedView === "application") {
            // Fetch all data from api and initialize stores
            const actualAppFootprint = this.footprintRepo.getValueApplicationFootprint();

            let currentInventoryId = null;
            for (const key in actualAppFootprint) {
                currentInventoryId = actualAppFootprint[key].id || "undefined";
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

    initFiltersApplication(inventoryId: number): Observable<FilterApplication> {
        const appGraph = this.footprintRepo.getValueAppGraphPositionStore();

        return this.footprintDataService
            .getApplicationFilters(
                inventoryId,
                appGraph.domain,
                appGraph.subdomain,
                appGraph.app,
            )
            .pipe(
                map(this.cleanApplicationFilters),
                tap(this.filterRepo.setAllFiltersApp),
                tap(this.filterRepo.updateSelectedFiltersApp),
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
                    this.footprintRepo.setPhysicalEquipmentStats(averageAges, lowImpact),
                ),
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
                        `criteria.${indicateur.criteria}.title`,
                    );
                    indicateur.id = inventoryId;
                });
                this.footprintRepo.setApplicationFootprint(footprint);
            }),
        );
    }

    initApplicationCriteriaFootprint(inventoryId: number, app: string, criteria: string) {
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
                            `criteria.${indicateur.criteria}.title`,
                        );
                    });
                    this.footprintRepo.setApplicationCriteriaFootprint(footprint);
                }),
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

    private getUnitFromCriteria(criteria: string): string {
        return this.translate.instant(`criteria.${criteria}.unite`);
    }

    private cleanApplicationFilters(
        rawFilters: FilterApplicationReceived,
    ): FilterApplication {
        const transformedFilters: FilterApplication = {
            environments: [...rawFilters.environments],
            types: [...rawFilters.types],
            lifeCycles: [],
            domains: [],
        };

        const lifecyleMap = LifeCycleUtils.getLifeCycleMap();

        transformedFilters.lifeCycles = rawFilters.lifeCycles.map(
            (lifeCycle) => lifecyleMap.get(lifeCycle) || lifeCycle,
        );

        transformedFilters.domains = rawFilters.domains.map((domain) => {
            if (domain.name === "") {
                domain.name = Constants.UNSPECIFIED;
            }

            let domainString = domain.name;
            const subDomains = domain.subDomains
                .map((subdomain) =>
                    subdomain === "" ? Constants.UNSPECIFIED : subdomain,
                )
                .join(",");

            if (subDomains.length > 0) {
                domainString += "," + subDomains;
            }

            return domainString;
        });

        // Replace null values by "Empty" and add Constants.ALL value
        return Object.keys(transformedFilters).reduce((acc, key) => {
            acc[key as keyof FilterApplicationReceived] = [
                Constants.ALL,
                ...transformedFilters[key as keyof FilterApplicationReceived]
                    .map((item: any) => item || Constants.UNSPECIFIED)
                    .sort(),
            ];
            return acc;
        }, {} as FilterApplication);
    }

    addImpact(i1: SumImpact, i2: SumImpact) {
        return {
            impact: i1.impact + i2.impact,
            sip: i1.sip + i2.sip,
        };
    }

    calculate(
        footprint: Criterias,
        filters: Filter,
        selectedView: string,
        filterFields: string[],
    ): FootprintCalculated[] {
        if (footprint === undefined) return [];

        const footprintCalculated: FootprintCalculated[] = [];

        const order = LifeCycleUtils.getLifeCycleList();
        const lifeCycleMap = LifeCycleUtils.getLifeCycleMap();

        const filtersSet: any = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        for (let criteria in footprint) {
            if (!footprint[criteria].impacts) continue;

            const filteredImpacts = hasAllFilters
                ? footprint[criteria].impacts
                : footprint[criteria].impacts.filter((impact: Impact) => {
                      let isPresent = true;
                      for (const field in filtersSet) {
                          let value = this.valueImpact(impact, field)!;
                          if (value == null) value = Constants.EMPTY;

                          if (!filtersSet[field].has(value)) {
                              isPresent = false;
                              break;
                          }
                      }
                      return isPresent;
                  });

            const groupedSumImpacts = new Map<string, SumImpact>();

            for (const impact of filteredImpacts) {
                let key = this.valueImpact(impact, selectedView)!;
                if (key == null) key = Constants.EMPTY;
                groupedSumImpacts.set(
                    key,
                    this.addImpact(
                        groupedSumImpacts.get(key) || { impact: 0, sip: 0 },
                        impact,
                    ),
                );
            }

            for (let [dimension, sumImpact] of groupedSumImpacts) {
                const impact = {
                    criteria,
                    sumSip: sumImpact.sip,
                    sumImpact: sumImpact.impact,
                };

                const translated = lifeCycleMap.get(dimension);

                const view: FootprintCalculated = {
                    data: translated ? translated : dimension,
                    impacts: [impact],
                    total: {
                        impact: impact.sumImpact,
                        sip: impact.sumSip,
                    },
                };

                const viewExist = footprintCalculated.find(
                    (data: any) => data.data === view.data,
                );
                if (viewExist) {
                    viewExist.impacts.push(impact);
                    viewExist.total = this.addImpact(viewExist.total, view.total);
                } else {
                    footprintCalculated.push(view);
                }
            }
        }

        if (selectedView === Constants.ACV_STEP) {
            footprintCalculated.sort((a: any, b: any) => {
                return order.indexOf(a.data) - order.indexOf(b.data);
            });
        } else {
            // Sort by alphabetical order
            footprintCalculated.sort((a: any, b: any) => a.data.localeCompare(b.data));
        }

        return footprintCalculated;
    }

    valueImpact(v: Impact, dimension: string) {
        switch (dimension) {
            case Constants.ACV_STEP:
                return v.acvStep;
            case "country":
                return v.country;
            case "entity":
                return v.entity;
            case "equipment":
                return v.equipment;
            case "status":
                return v.status;
            default:
                return null;
        }
    }

    calculateTotal(footprintCalculated: FootprintCalculated[], unit: string) {
        return footprintCalculated.reduce(
            (sum, current) =>
                sum +
                (unit === Constants.PEOPLEEQ ? current.total.sip : current.total.impact),
            0,
        );
    }
}
