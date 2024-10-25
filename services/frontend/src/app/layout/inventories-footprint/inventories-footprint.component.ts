/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit, inject } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { finalize, firstValueFrom } from "rxjs";
import { Filter } from "src/app/core/interfaces/filter.interface";
import {
    ChartData,
    ComputedSelection,
    Criteria,
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "src/app/core/interfaces/footprint.interface";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { GlobalStoreService } from "src/app/core/store/global.store";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-footprint",
    templateUrl: "./inventories-footprint.component.html",
})
export class InventoriesFootprintComponent implements OnInit {
    protected footprintStore = inject(FootprintStoreService);
    private global = inject(GlobalStoreService);

    selectedView: string = "";

    echartsData: any = [];

    chartData: ChartData<ComputedSelection> = {};

    selectedLang: string = this.translate.currentLang;

    criterias = [Constants.MUTLI_CRITERIA, ...Object.keys(this.global.criteriaList())];

    criteres: MenuItem[] = [{ label: "Multi-criteria", routerLink: "../multi-criteria" }];

    allUnmodifiedFootprint: Criterias = {} as Criterias;
    allUnmodifiedFilters: Filter<string> = {};
    allUnmodifiedDatacenters: Datacenter[] = [] as Datacenter[];
    allUnmodifiedEquipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];
    allUnmodifiedCriteriaFootprint: Criteria = {} as Criteria;

    order = LifeCycleUtils.getLifeCycleList();
    lifeCycleMap = LifeCycleUtils.getLifeCycleMap();

    filterFields = Constants.EQUIPMENT_FILTERS;
    multiCriteria = Constants.MUTLI_CRITERIA;
    inventoryId = 0;
    showTabMenu = false;
    constructor(
        private activatedRoute: ActivatedRoute,
        private footprintDataService: FootprintDataService,
        private footprintService: FootprintService,
        private translate: TranslateService,
    ) {}

    async ngOnInit() {
        const criteria = this.activatedRoute.snapshot.paramMap.get("criteria");
        this.global.setLoading(true);
        // Set active inventory based on route
        this.inventoryId =
            +this.activatedRoute.snapshot.paramMap.get("inventoryId")! || 0;

        this.footprintDataService
            .getFootprint(this.inventoryId)
            .pipe(finalize(() => (this.showTabMenu = true)))
            .subscribe((criterias: Criterias) => {
                this.criteres = Object.entries(criterias).map(
                    ([key, criteria]: [string, Criteria]) => {
                        return {
                            label: this.translate.instant(`criteria.${key}.title`),
                            routerLink: `../${key}`,
                        };
                    },
                );
                if (this.criteres.length > 1) {
                    this.criteres.unshift({
                        label: "Multi-criteria",
                        routerLink: "../multi-criteria",
                    });
                }
            });

        this.footprintStore.setCriteria(criteria || Constants.MUTLI_CRITERIA);

        const [footprint, datacenters, physicalEquipments] = await Promise.all([
            firstValueFrom(this.footprintDataService.getFootprint(this.inventoryId)),
            firstValueFrom(this.footprintDataService.getDatacenters(this.inventoryId)),
            firstValueFrom(
                this.footprintDataService.getPhysicalEquipments(this.inventoryId),
            ),
        ]);

        this.allUnmodifiedFootprint = JSON.parse(JSON.stringify(footprint));
        this.allUnmodifiedDatacenters = datacenters;
        this.allUnmodifiedEquipments = physicalEquipments;
        this.allUnmodifiedFilters = {};

        const uniqueFilterSet = this.footprintService.getUniqueValues(
            this.allUnmodifiedFootprint,
            Constants.EQUIPMENT_FILTERS,
            true,
        );

        Constants.EQUIPMENT_FILTERS.forEach((field) => {
            this.allUnmodifiedFilters[field] = [
                Constants.ALL,
                ...uniqueFilterSet[field]
                    .map((item: any) => (item ? item : Constants.EMPTY))
                    .sort(),
            ];
        });

        this.global.setLoading(false);

        // React on criteria url param change
        this.activatedRoute.paramMap.subscribe((params) => {
            const criteria = params.get("criteria")!;
            this.footprintStore.setCriteria(criteria);

            if (criteria !== Constants.MUTLI_CRITERIA) {
                this.allUnmodifiedCriteriaFootprint =
                    this.allUnmodifiedFootprint[criteria];
            }
        });
    }
}
