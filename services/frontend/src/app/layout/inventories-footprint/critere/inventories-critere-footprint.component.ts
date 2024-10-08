/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, Signal, computed, inject, signal } from "@angular/core";

import { EChartsOption } from "echarts";
import {
    Criteria,
    Criterias,
    EchartPieDataItem,
    FootprintCalculated,
} from "src/app/core/interfaces/footprint.interface";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { getLifeCycleList, getLifeCycleMap } from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-inventories-critere-footprint",
    templateUrl: "./inventories-critere-footprint.component.html",
})
export class InventoriesCritereFootprintComponent extends AbstractDashboard {
    protected footprintStore = inject(FootprintStoreService);
    private footprintService = inject(FootprintService);

    @Input() footprint: Criteria = {} as Criteria;
    @Input() filterFields: string[] = [];

    dimensions = Constants.EQUIPMENT_DIMENSIONS;
    selectedDimension = signal(this.dimensions[0]);

    totalValue = 0;

    options: Signal<EChartsOption> = computed(() => {
        const criteria = this.footprintStore.criteria();

        const footprint: Criterias = {};
        footprint[criteria] = this.footprint;

        const footprintCalculated = this.footprintService.calculate(
            footprint,
            this.footprintStore.filters(),
            this.selectedDimension(),
            this.filterFields,
        );

        this.totalValue = this.footprintService.calculateTotal(
            footprintCalculated,
            this.footprintStore.unit(),
        );

        return this.renderChart(
            footprintCalculated,
            this.selectedDimension(),
            this.footprintStore.unit(),
        );
    });

    unitOfCriteria = computed(() =>
        this.translate.instant(`criteria.${this.footprintStore.criteria()}.unite`),
    );

    changeUnitInStore(unit: string) {
        this.footprintStore.setUnit(unit);
    }

    infocard = computed(() => {
        return {
            title: this.translate.instant(
                `criteria.${this.footprintStore.criteria()}.inventory-title`,
            ),
            text: this.translate.instant(
                `criteria.${this.footprintStore.criteria()}.inventory-text`,
            ),
        };
    });

    renderChart(
        footprintCalculated: FootprintCalculated[],
        selectedView: string,
        unit: string,
    ): EChartsOption {
        if (footprintCalculated.length === 0) {
            return {};
        }

        const lifecycleMap = getLifeCycleMap();
        const echartsData: EchartPieDataItem[] = [];
        const otherData: any = [];

        const total = footprintCalculated.reduce(
            (sum, current) =>
                sum +
                (unit === Constants.PEOPLEEQ ? current.total.sip : current.total.impact),
            0,
        );
        footprintCalculated.forEach((item) => {
            const sumValue =
                unit === Constants.PEOPLEEQ ? item.total.sip : item.total.impact;
            const translated = lifecycleMap.get(item.data);
            const v: any = {
                value: sumValue,
                name: translated ? translated : item.data,
            };
            var percent = (sumValue / total) * 100;
            if (percent < 1) v.percent = percent;
            percent < 1 ? otherData.push(v) : echartsData.push(v);
        });

        // Push the single data entry for multiple entities with impact less than 1%.
        if (otherData.length > 0) {
            echartsData.push({
                name: "other",
                value: otherData
                    .map((data: any) => data.value)
                    .reduce((sum: number, current: number) => sum + current, 0),
                otherData: otherData.sort((a: any, b: any) => a.value < b.value),
            });
        }
        if (selectedView == Constants.ACV_STEP) {
            echartsData.sort((a: any, b: any) => {
                return (
                    getLifeCycleList().indexOf(a.name) -
                    getLifeCycleList().indexOf(b.name)
                );
            });
        } else {
            // Sort by alphabetical order
            echartsData.sort((a: any, b: any) => a.name.localeCompare(b.name));
        }

        return {
            height: 700,
            tooltip: {
                enterable: true,
                hideDelay: 200,
                trigger: "item",
                formatter: (params: any) => {
                    return this.getToolTipHtml(
                        echartsData[params.dataIndex],
                        unit === Constants.PEOPLEEQ
                            ? Constants.PEOPLEEQ
                            : this.unitOfCriteria(),
                        params.color,
                        selectedView,
                    );
                },
            },
            legend: {
                show: true,
                type: "scroll",
                bottom: -5,
                selectedMode: false,
                formatter: (param: any) => {
                    return this.existingTranslation(param, selectedView, "legend");
                },
            },
            series: [
                {
                    avoidLabelOverlap: false,
                    type: "pie",
                    radius: ["50%", "75%"],
                    center: ["50%", "45%"],
                    // adjust the start angle
                    startAngle: 180,
                    endAngle: 360,
                    label: {
                        show: true,
                        formatter: (param: any) => {
                            // correct the percentage
                            return `${this.existingTranslation(
                                param.name,
                                selectedView,
                            )} ${param.percent.toFixed(1)}%`;
                        },
                    },
                    data: echartsData,
                },
            ],
            color: Constants.COLOR,
        };
    }

    getToolTipHtml(chartValue: any, unit: string, color: any, selectedView: string) {
        let height = 22;
        let maxHeight = 250;
        let tooltipLines = [];

        const translatedUnit =
            unit === Constants.PEOPLEEQ
                ? this.translate.instant(
                      `common.${Constants.PEOPLEEQ.toLocaleLowerCase()}`,
                  )
                : unit;

        const pipe = unit === Constants.PEOPLEEQ ? this.integerPipe : this.decimalsPipe;

        if (chartValue.name.toLowerCase() === "other") {
            tooltipLines = chartValue.otherData.map((data: any) =>
                this.getTooltipItemHtml(
                    data.name,
                    `${data.percent.toFixed(3)}% - ${pipe.transform(
                        data.value,
                    )} ${translatedUnit}`,
                ),
            );
        } else {
            tooltipLines = [
                this.getTooltipItemHtml(
                    this.existingTranslation(chartValue.name, selectedView),
                    `${pipe.transform(chartValue.value)} ${translatedUnit}`,
                    color,
                ),
            ];
        }

        height = tooltipLines.length <= 10 ? height * tooltipLines.length : maxHeight;
        const titleDiv =
            tooltipLines.length > 1
                ? `${this.getCircleColorHtml(color)} ${this.translate.instant(
                      "common.otherLegend",
                  )}`
                : "";

        return `
            ${titleDiv}
            <div class="mt-1" style="display: flex; flex-direction: column; align-items: baseline; height: ${height}px; overflow-y:auto">
                ${tooltipLines.join("")}
            </div>
        `;
    }

    getCircleColorHtml(color: string | undefined = undefined) {
        return color
            ? `<div class="mr-1 pb-1 inline-block" style="width: 9px; height: 9px; background-color: ${color}; border-radius: 50%;"></div>`
            : "";
    }

    getTooltipItemHtml(
        name: string,
        value: string,
        color: string | undefined = undefined,
    ) {
        return `
            <div class="ml-1 mr-2">
                ${this.getCircleColorHtml(color)}
                <div class="font-bold">${name}:</div>
                <div class="ml-2">${value}</div>
            </div>
         `;
    }
}
