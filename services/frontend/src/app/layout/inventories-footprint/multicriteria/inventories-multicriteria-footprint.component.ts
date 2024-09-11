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
import {
    Criterias,
    Datacenter,
    PhysicalEquipment,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
} from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";

import {
    FootprintCalculated,
    Impact,
    Stat,
} from "src/app/core/interfaces/footprint.interface";
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

    @Input() footprint: Criterias = {} as Criterias;
    @Input() filterFields: string[] = [];
    @Input() datacenters: Datacenter[] = [];
    @Input() equipments: [PhysicalEquipmentAvgAge[], PhysicalEquipmentLowImpact[]] = [
        [],
        [],
    ];

    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    selectedDimension = signal(this.dimensions[0]);

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
                    Constants.CRITERIAS.indexOf(a.criteria) -
                    Constants.CRITERIAS.indexOf(b.criteria),
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
                        formatter: (params: any) => {
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

    valueEquipment(v: PhysicalEquipment, field: string) {
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

    valueEquipmentLowImpact(v: PhysicalEquipmentLowImpact, field: string) {
        switch (field) {
            case "country":
                return v.paysUtilisation;
            case "nomEntite":
                return v.nomEntite;
            case "equipment":
                return v.type;
            case "status":
                return v.statut;
            default:
                return null;
        }
    }

    computeDataCenterStats(
        datacenters: Datacenter[] = [],
        filters: Filter,
        filterFields: string[],
    ): Stat[] {
        if (datacenters.length === 0) return this.getDatacenterStats();

        const filtersSet: any = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        const filteredDatacenters = hasAllFilters
            ? datacenters
            : datacenters.filter((datacenter) => {
                  let isPresent = true;
                  for (const field in filtersSet) {
                      let value = this.valueDatacenter(datacenter, field)!;
                      if (!value) value = Constants.EMPTY;
                      if (!filtersSet[field].has(value)) {
                          isPresent = false;
                          break;
                      }
                  }
                  return isPresent;
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
        equipments: [PhysicalEquipmentAvgAge[], PhysicalEquipmentLowImpact[]],
        filters: Filter,
        filterFields: string[],
    ): Stat[] {
        const equipmentsAvgAge = equipments[0];
        const equipmentsLowImpact = equipments[1];

        if (equipmentsAvgAge.length === 0 && equipmentsLowImpact.length === 0)
            return this.getEquipmentStats();

        const filtersSet: any = {};
        filterFields.forEach((field) => (filtersSet[field] = new Set(filters[field])));

        const hasAllFilters = Object.keys(filtersSet).every((item) =>
            filtersSet[item].has(Constants.ALL),
        );

        const filteredEquipmentsAvgAge = hasAllFilters
            ? equipmentsAvgAge
            : equipmentsAvgAge.filter((equipment) => {
                  let isPresent = true;
                  for (const field in filtersSet) {
                      let value = this.valueEquipment(equipment, field)!;
                      if (!value) value = Constants.EMPTY;
                      if (!filtersSet[field].has(value)) {
                          isPresent = false;
                          break;
                      }
                  }
                  return isPresent;
              });

        let physicalEquipmentSum = 0;
        let physicalEquipmentCount = 0;

        filteredEquipmentsAvgAge.forEach((physicalEquipment) => {
            let { poids, ageMoyen } = physicalEquipment;
            poids = poids || 0;
            ageMoyen = ageMoyen || 0;

            physicalEquipmentCount += poids;
            physicalEquipmentSum += poids * ageMoyen;
        });

        const filteredEquipmentsLowImpact = hasAllFilters
            ? equipmentsLowImpact
            : equipmentsLowImpact.filter((equipment) => {
                  let isPresent = true;
                  for (const field in filtersSet) {
                      let value = this.valueEquipmentLowImpact(equipment, field)!;
                      if (!value) value = Constants.EMPTY;
                      if (!filtersSet[field].has(value)) {
                          isPresent = false;
                          break;
                      }
                  }
                  return isPresent;
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

        return this.getEquipmentStats(
            physicalEquipmentCount,
            physicalEquipmentSum / physicalEquipmentCount,
            (lowImpactPhysicalEquipmentCount / physicalEquipmentTotalCount) * 100,
        );
    }

    getEquipmentStats(
        count: number = NaN,
        avgAge: number = NaN,
        lowImpact: number = NaN,
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
        ];
    }
}
