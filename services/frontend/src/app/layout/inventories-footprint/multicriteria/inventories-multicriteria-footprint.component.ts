/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, Signal, computed, inject, signal } from "@angular/core";
import { EChartsOption } from "echarts";
import { Filter } from "src/app/core/interfaces/filter.interface";
import { Constants } from "src/constants";

import {
    Criterias,
    Datacenter,
    FootprintCalculated,
    Impact,
    PhysicalEquipment,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
    PhysicalEquipmentsElecConsumption,
    Stat,
} from "src/app/core/interfaces/footprint.interface";
import { InventoryFilterSet } from "src/app/core/interfaces/inventory.interfaces";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-inventories-multicriteria-footprint",
    templateUrl: "./inventories-multicriteria-footprint.component.html",
})
export class InventoriesMultiCriteriaFootprintComponent extends AbstractDashboard {
    private store = inject(FootprintStoreService);
    private footprintService = inject(FootprintService);
    currentLang: string = this.translate.currentLang;
    criteriakeys = Object.keys(this.translate.translations[this.currentLang].criteria);
    @Input() footprint: Criterias = {} as Criterias;
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() equipments: [
        PhysicalEquipmentAvgAge[],
        PhysicalEquipmentLowImpact[],
        PhysicalEquipmentsElecConsumption[],
    ] = [[], [], []];

    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    selectedDimension = signal(this.dimensions[0]);

    maxCriteriaAndStep: Signal<string[]> = computed(() => {
        let maxCriteriaLength = -1;
        let maxCriteria = "climate-change";

        for (let criteria in this.footprint) {
            if (!this.footprint[criteria] || !this.footprint[criteria].impacts) continue;
            const criteriaLength = this.footprint[criteria].impacts.length;
            if (criteriaLength > maxCriteriaLength) {
                maxCriteriaLength = criteriaLength;
                maxCriteria = criteria;
            }
        }

        let maxStepLength = -1;
        let maxStep = "UTILISATION";

        const sizeBySteps = this.footprint[maxCriteria].impacts.reduce((p: any, c) => {
            var name = c.acvStep;
            if (!p.hasOwnProperty(name)) {
                p[name] = 0;
            }
            p[name]++;
            return p;
        }, {});

        for (const step in sizeBySteps) {
            if (sizeBySteps[step] > maxStepLength) {
                maxStepLength = sizeBySteps[step];
                maxStep = step;
            }
        }

        return [maxCriteria, maxStep];
    });

    options: Signal<EChartsOption> = computed(() => {
        const footprintCalculated = this.footprintService.calculate(
            this.footprint,
            this.store.filters(),
            this.selectedDimension(),
            this.filterFields,
        );

        // sort footprint by criteria
        footprintCalculated.forEach((data) => {
            data.impacts.sort(
                (a, b) =>
                    this.criteriakeys.indexOf(a.criteria) -
                    this.criteriakeys.indexOf(b.criteria),
            );
        });

        return this.renderChart(footprintCalculated, this.selectedDimension());
    });

    datacenterStats = computed<Stat[]>(() =>
        this.computeDataCenterStats(
            this.datacenters,
            this.store.filters(),
            this.filterFields,
        ),
    );

    equipmentStats = computed<Stat[]>(() =>
        this.computeEquipmentStats(
            this.equipments,
            this.store.filters(),
            this.filterFields,
            this.footprint,
        ),
    );

    renderChart(
        footprintCalculated: FootprintCalculated[],
        selectedView: string,
    ): EChartsOption {
        if (footprintCalculated.length === 0) {
            return {};
        }

        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    const seriesIndex = params.seriesIndex;
                    const impact = footprintCalculated[seriesIndex].impacts[
                        dataIndex
                    ] as Impact;
                    const name = this.existingTranslation(
                        footprintCalculated[seriesIndex].data,
                        selectedView,
                    );
                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                                params.color
                            }; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                            <div>${this.translate.instant(`criteria.${impact.criteria}`).title} : ${this.integerPipe.transform(
                                impact.sumSip,
                            )} ${this.translate.instant("common.peopleeq-min")} </div>
                        </div>
                    `;
                },
            },
            angleAxis: {
                type: "category",
                data: footprintCalculated[0].impacts.map(
                    (impact: Impact) =>
                        this.translate.instant(`criteria.${impact.criteria}`).title,
                ),
            },
            radiusAxis: {
                name: this.translate.instant("common.peopleeq"),
                nameLocation: "end",
                nameTextStyle: {
                    fontStyle: "italic",
                },
            },
            polar: {
                center: ["50%", "47%"],
            },
            series: footprintCalculated.map((item: FootprintCalculated) => ({
                name: item.data,
                type: "bar",
                coordinateSystem: "polar",
                data: item.impacts.map((impact: Impact) => ({
                    value: impact.sumSip,
                    label: {
                        formatter: () => {
                            return `${impact.sumImpact} ${this.translate.instant(`criteria.${impact.criteria}`).unit}`;
                        },
                    },
                })),
                stack: "a",
                emphasis: {
                    focus: "series",
                },
            })),
            avoidLabelOverlap: true,
            legend: {
                type: "scroll",
                bottom: 0,
                data: footprintCalculated.map((item: FootprintCalculated) => item.data),
                formatter: (param: any) => {
                    return this.existingTranslation(param, selectedView);
                },
            },
            color: Constants.COLOR,
        };
    }

    valueDatacenter(v: Datacenter, field: string) {
        switch (field) {
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

    valueEquipment(
        v:
            | PhysicalEquipment
            | PhysicalEquipmentLowImpact
            | PhysicalEquipmentsElecConsumption,
        field: string,
        islowImpact: boolean,
    ) {
        switch (field) {
            case "country":
                return v.country;
            case "entity":
                return v.nomEntite;
            case "equipment":
                return v.type;
            case "status":
                return v.statut;
            default:
                return null;
        }
    }

    valueImpact(v: Impact, field: string, islowImpact: boolean) {
        switch (field) {
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

    computeDataCenterStats(
        datacenters: Datacenter[] = [],
        filters: Filter<string>,
        filterFields: string[],
    ): Stat[] {
        if (datacenters.length === 0) return this.getDatacenterStats();

        const filtersSet: InventoryFilterSet = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        const filteredDatacenters = hasAllFilters
            ? datacenters
            : datacenters.filter((datacenter) => {
                  return this.isDataCenterPresent(datacenter, filtersSet);
              });

        let datacenterPhysicalEquipmentCount = 0;
        let datacenterSum = 0;
        const datacenterNames = new Set<string>();

        filteredDatacenters.forEach((datacenter) => {
            let { dataCenterName, physicalEquipmentCount, pue } = datacenter;

            pue = pue || 0;
            dataCenterName = dataCenterName || "";
            physicalEquipmentCount = physicalEquipmentCount || 0;

            datacenterPhysicalEquipmentCount += physicalEquipmentCount;
            datacenterSum += physicalEquipmentCount * pue;
            datacenterNames.add(dataCenterName);
        });

        return this.getDatacenterStats(
            datacenterNames.size,
            datacenterSum / datacenterPhysicalEquipmentCount,
        );
    }

    getDatacenterStats(count: number = NaN, avgPue: number = NaN) {
        return [
            {
                label: this.decimalsPipe.transform(count),
                value: isNaN(count) ? undefined : count,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.nb-dc",
                ),
                title: this.translate.instant("inventories-footprint.global.datacenters"),
            },
            {
                label: this.decimalsPipe.transform(avgPue),
                value: isNaN(avgPue) ? undefined : avgPue,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.ave-pue",
                ),
                title: this.translate.instant("inventories-footprint.global.ave-pue"),
            },
        ];
    }

    computeEquipmentStats(
        equipments: [
            PhysicalEquipmentAvgAge[],
            PhysicalEquipmentLowImpact[],
            PhysicalEquipmentsElecConsumption[],
        ],
        filters: Filter<string>,
        filterFields: string[],
        footprint: Criterias,
    ): Stat[] {
        const equipmentsAvgAge = equipments[0];
        const equipmentsLowImpact = equipments[1];
        const equipmentsElecConsumption = equipments[2];

        if (
            equipmentsAvgAge.length === 0 &&
            equipmentsLowImpact.length === 0 &&
            equipmentsElecConsumption.length === 0
        )
            return this.getEquipmentStats();

        const filtersSet: InventoryFilterSet = {};
        filterFields.forEach(
            (field) => (filtersSet[field as string] = new Set(filters[field as string])),
        );

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        const impacts = footprint[this.maxCriteriaAndStep()[0]].impacts.filter(
            (impact) => impact.acvStep === this.maxCriteriaAndStep()[1],
        );

        const physicalEquipmentCount = hasAllFilters
            ? impacts.reduce((n, impact) => n + impact.countValue, 0)
            : impacts
                  .filter((impact) => this.isImpactPresent(impact, filtersSet))
                  .reduce((n, impact) => n + impact.countValue, 0);

        const filteredEquipmentsAvgAge = hasAllFilters
            ? equipmentsAvgAge
            : equipmentsAvgAge.filter((equipment) => {
                  return this.isEquipmentPresent(equipment, filtersSet, false);
              });

        let physicalEquipmentSum = 0;

        filteredEquipmentsAvgAge.forEach((physicalEquipment) => {
            let { poids, ageMoyen } = physicalEquipment;
            poids = poids || 0;
            ageMoyen = ageMoyen || 0;

            physicalEquipmentSum += poids * ageMoyen;
        });

        const filteredEquipmentsLowImpact = hasAllFilters
            ? equipmentsLowImpact
            : equipmentsLowImpact.filter((equipment) => {
                  return this.isEquipmentPresent(equipment, filtersSet, true);
              });

        let physicalEquipmentTotalCount = 0;
        let lowImpactPhysicalEquipmentCount = 0;

        filteredEquipmentsLowImpact.forEach((physicalEquipment) => {
            let { quantite, lowImpact: isLowImpact } = physicalEquipment;

            quantite = quantite || 0;

            physicalEquipmentTotalCount += quantite;
            if (isLowImpact) {
                lowImpactPhysicalEquipmentCount += quantite;
            }
        });

        const filteredEquipmentsElecConsumption = hasAllFilters
            ? equipmentsElecConsumption
            : equipmentsElecConsumption.filter((equipment) => {
                  return this.isEquipmentPresent(equipment, filtersSet, false);
              });

        let elecConsumptionSum = 0;

        filteredEquipmentsElecConsumption.forEach((physicalEquipment) => {
            let { elecConsumption } = physicalEquipment;

            elecConsumptionSum += elecConsumption;
        });

        return this.getEquipmentStats(
            physicalEquipmentCount,
            physicalEquipmentCount == 0
                ? undefined
                : physicalEquipmentSum / physicalEquipmentCount,
            (lowImpactPhysicalEquipmentCount / physicalEquipmentTotalCount) * 100,
            elecConsumptionSum,
        );
    }

    isPresent<
        T extends
            | Impact
            | Datacenter
            | PhysicalEquipment
            | PhysicalEquipmentLowImpact
            | PhysicalEquipmentsElecConsumption,
    >(
        item: T,
        filtersSet: InventoryFilterSet,
        islowImpact: boolean,
        valueFn: (v: T, field: string, islowImpact: boolean) => string | null,
    ): boolean {
        let isPresent = true;
        for (const field in filtersSet) {
            let value = valueFn(item, field, islowImpact)!;
            if (!value) value = Constants.EMPTY;
            if (!filtersSet[field].has(value)) {
                isPresent = false;
                break;
            }
        }
        return isPresent;
    }

    isDataCenterPresent(datacenter: Datacenter, filtersSet: InventoryFilterSet) {
        return this.isPresent(datacenter, filtersSet, false, this.valueDatacenter);
    }

    isEquipmentPresent(
        equipment:
            | PhysicalEquipment
            | PhysicalEquipmentLowImpact
            | PhysicalEquipmentsElecConsumption,
        filtersSet: InventoryFilterSet,
        islowImpact: boolean,
    ): boolean {
        return this.isPresent(equipment, filtersSet, islowImpact, this.valueEquipment);
    }

    isImpactPresent(impact: Impact, filtersSet: InventoryFilterSet): boolean {
        return this.isPresent(impact, filtersSet, false, this.valueImpact);
    }

    getEquipmentStats(
        count: number = NaN,
        avgAge: number = NaN,
        lowImpact: number = NaN,
        elecConsumption: number = NaN,
    ) {
        return [
            {
                label: this.decimalsPipe.transform(count),
                value: isNaN(count) ? undefined : count,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.nb-eq",
                ),
                title: this.translate.instant("inventories-footprint.global.equipments"),
            },
            {
                label: this.decimalsPipe.transform(avgAge),
                value: isNaN(avgAge) ? undefined : avgAge,
                unit: this.translate.instant("inventories-footprint.global.years"),
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.ave-age",
                ),
                title: this.translate.instant("inventories-footprint.global.ave-age"),
            },
            {
                label: `${this.integerPipe.transform(lowImpact)}%`,
                value: isNaN(lowImpact) ? undefined : lowImpact,
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.low-impact",
                ),
                title: this.translate.instant("inventories-footprint.global.low-impact"),
            },
            {
                label: `${this.decimalsPipe.transform(elecConsumption)}`,
                unit: this.translate.instant("inventories-footprint.global.kwh"),
                value: isNaN(elecConsumption) ? undefined : Math.round(elecConsumption),
                description: this.translate.instant(
                    "inventories-footprint.global.tooltip.elec-consumption",
                ),
                title: this.translate.instant(
                    "inventories-footprint.global.elec-consumption",
                ),
            },
        ];
    }
}
