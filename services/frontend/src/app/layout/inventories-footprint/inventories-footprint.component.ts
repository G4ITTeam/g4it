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
    Impact,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
} from "src/app/core/interfaces/footprint.interface";
import { InVirtualEquipmentRest } from "src/app/core/interfaces/input.interface";
import { OutVirtualEquipmentRest } from "src/app/core/interfaces/output.interface";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { InVirtualEquipmentsService } from "src/app/core/service/data/in-out/in-virtual-equipments.service";
import { OutVirtualEquipmentsService } from "src/app/core/service/data/in-out/out-virtual-equipments.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";
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
    private outVirtualEquipmentService = inject(OutVirtualEquipmentsService);
    private inVirtualEquipmentsService = inject(InVirtualEquipmentsService);
    private digitalServiceStore = inject(DigitalServiceStoreService);

    selectedView: string = "";

    echartsData: any = [];

    chartData: ChartData<ComputedSelection> = {};

    selectedLang: string = this.translate.currentLang;

    criterias = [Constants.MUTLI_CRITERIA, ...Object.keys(this.global.criteriaList())];

    criteres: MenuItem[] = [
        {
            label: this.translate.instant("criteria-title.multi-criteria.title"),
            routerLink: `../${Constants.MUTLI_CRITERIA}`,
            id: "multi-criteria",
        },
    ];

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
    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    transformedInVirtualEquipments: InVirtualEquipmentRest[] = [];
    constructor(
        private activatedRoute: ActivatedRoute,
        private footprintDataService: FootprintDataService,
        private footprintService: FootprintService,
        private translate: TranslateService,
        private digitalBusinessService: DigitalServiceBusinessService,
    ) {}

    async ngOnInit() {
        const criteria = this.activatedRoute.snapshot.paramMap.get("criteria");
        this.global.setLoading(true);
        this.digitalBusinessService.initCountryMap();
        // Set active inventory based on route
        this.inventoryId =
            +this.activatedRoute.snapshot.paramMap.get("inventoryId")! || 0;

        this.footprintDataService
            .getFootprint(this.inventoryId)
            .pipe(finalize(() => (this.showTabMenu = true)))
            .subscribe((criterias: Criterias) => {
                const footprintCriteriaKeys = Object.keys(criterias);
                const sortedCriteriaKeys = Object.keys(this.global.criteriaList()).filter(
                    (key) => footprintCriteriaKeys.includes(key),
                );
                this.criteres = sortedCriteriaKeys.map((key: string) => {
                    return {
                        label: this.translate.instant(`criteria.${key}.title`),
                        routerLink: `../${key}`,
                        id: `${key}`,
                    };
                });
                if (this.criteres.length > 1) {
                    this.criteres.unshift({
                        label: this.translate.instant(
                            "criteria-title.multi-criteria.title",
                        ),
                        routerLink: `../${Constants.MUTLI_CRITERIA}`,
                    });
                }
            });

        this.footprintStore.setCriteria(criteria || Constants.MUTLI_CRITERIA);

        const [
            footprint,
            datacenters,
            physicalEquipments,
            outVirtualEquipments,
            inVirtualEquipments,
        ] = await Promise.all([
            firstValueFrom(this.footprintDataService.getFootprint(this.inventoryId)),
            firstValueFrom(this.footprintDataService.getDatacenters(this.inventoryId)),
            firstValueFrom(
                this.footprintDataService.getPhysicalEquipments(this.inventoryId),
            ),
            firstValueFrom(
                this.outVirtualEquipmentService.getByInventory(this.inventoryId),
            ),
            firstValueFrom(
                this.inVirtualEquipmentsService.getByInventory(this.inventoryId),
            ),
        ]);
        this.transformedInVirtualEquipments =
            this.transformInVirtualEquipment(inVirtualEquipments);
        const transformedOutVirtualEquipments =
            this.transformOutVirtualEquipment(outVirtualEquipments);

        this.tranformAcvStepFootprint(footprint);

        transformedOutVirtualEquipments.forEach((equipment) => {
            const matchedFootprint = footprint[equipment.criteria];

            if (matchedFootprint) {
                matchedFootprint.impacts.push(equipment);
            }
        });
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

    transformOutVirtualEquipment(
        outVirtualEquipments: OutVirtualEquipmentRest[],
    ): Impact[] {
        return outVirtualEquipments
            .filter((item) => item.infrastructureType === "CLOUD_SERVICES")
            .map(
                (item) =>
                    ({
                        criteria: item.criterion.toLocaleLowerCase().replaceAll("_", "-"),
                        acvStep: LifeCycleUtils.getLifeCycleMapReverse().get(
                            item.lifecycleStep,
                        ),
                        country: this.digitalServiceStore.countryMap()[item.location],
                        equipment: `Cloud ${item.provider.toUpperCase()}`,
                        status: Constants.CLOUD_SERVICES,
                        entity: item.commonFilters?.[0] ?? null,
                        impact: item.unitImpact,
                        sip: item.peopleEqImpact,
                        statusIndicator: item.statusIndicator,
                        countValue: item.countValue,
                        quantity: item.quantity,
                    }) as Impact,
            );
    }

    transformInVirtualEquipment(
        inVirtualEquipments: InVirtualEquipmentRest[],
    ): InVirtualEquipmentRest[] {
        return inVirtualEquipments
            .filter((item) => item.infrastructureType === "CLOUD_SERVICES")
            .map((item) => ({
                ...item,
                country: this.digitalServiceStore.countryMap()[item.location],
                equipment: `Cloud ${item?.provider?.toUpperCase()}`,
                status: Constants.CLOUD_SERVICES,
                entity: item.commonFilters?.[0] ?? null,
                quantity: item.quantity,
            }));
    }

    tranformAcvStepFootprint(footprint: Criterias): void {
        for (const key in footprint) {
            if (
                footprint[key].impacts?.length &&
                LifeCycleUtils.getLifeCycleList().includes(
                    footprint[key]?.impacts[0]?.acvStep,
                )
            ) {
                footprint[key].impacts = footprint[key].impacts.map((i) => {
                    return {
                        ...i,
                        acvStep:
                            LifeCycleUtils.getLifeCycleMapReverse().get(i.acvStep) ??
                            i.acvStep,
                    };
                });
            }
        }
    }
}
