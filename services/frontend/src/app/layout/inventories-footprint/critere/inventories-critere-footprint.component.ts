/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { EChartsOption } from "echarts";
import { combineLatest, takeUntil } from "rxjs";

import { Constants } from "src/constants";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-inventories-critere-footprint",
    templateUrl: "./inventories-critere-footprint.component.html",
})
export class InventoriesCritereFootprintComponent
    extends AbstractDashboard
    implements OnInit
{
    unitOfCriteria: string = "";
    selectedCriteria: string = "";

    impact: number = 0;
    sip: number = 0;

    options: EChartsOption = {};

    echartsData: any = [];
    noData = false;

    ngOnInit(): void {
        combineLatest([this.echartsRepo.critereChart$, this.filterRepo.selectedView$])
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe(([chartData, selectedView]) => {
                // We always have one element representing half the donut.
                this.noData = chartData.length === 0;

                if (this.noData) {
                    return;
                }
                this.updateEchartsOptions(selectedView, chartData);
            });

        this.echartsRepo.unitOfCriteria$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((unit) => {
                this.unitOfCriteria = unit;
            });

        this.echartsRepo.criteriaImpact$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe(({ impact, sip }) => {
                this.impact = impact;
                this.sip = sip;
            });

        this.filterRepo.selectedCriteria$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((selectedCriteria) => {
                this.selectedCriteria = selectedCriteria;
            });
    }

    updateEchartsOptions(selectedView: string, echartsData: any[]) {
        this.options = {
            height: 700,
            tooltip: {
                enterable: true,
                hideDelay: 200,
                trigger: "item",
                formatter: (params: any) => {
                    return this.getToolTipHtml(
                        echartsData[params.dataIndex],
                        this.getUnitTranslation(this.selectedCriteria),
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

        if (chartValue.name.toLowerCase() === "other") {
            tooltipLines = chartValue.otherData.map((data: any) =>
                this.getTooltipItemHtml(
                    data.name,
                    `${data.percent.toFixed(3)}% - ${this.decimalsPipe.transform(
                        data.value,
                    )} ${unit}`,
                ),
            );
        } else {
            tooltipLines = [
                this.getTooltipItemHtml(
                    this.existingTranslation(chartValue.name, selectedView),
                    `${this.decimalsPipe.transform(chartValue.value)} ${unit}`,
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

    infoCardTitle(selectedCriteria: string | null) {
        if (!selectedCriteria) return "";
        return `inventories-footprint.critere.${selectedCriteria}.title`;
    }

    infoCardContent(selectedCriteria: string | null) {
        if (!selectedCriteria) return "";
        return `inventories-footprint.critere.${selectedCriteria}.text`;
    }

    getUnitTranslation(input: string) {
        return this.translate.instant(
            "inventories-footprint.critere." + input + ".unite",
        );
    }
}
