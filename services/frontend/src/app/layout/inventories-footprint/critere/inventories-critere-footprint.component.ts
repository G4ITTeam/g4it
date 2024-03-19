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
                this.noData = chartData.length === 1;

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
            tooltip: {
                trigger: "item",
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    const impact = echartsData[dataIndex].value.toFixed(0);
                    let name = this.existingTranslation(
                        echartsData[dataIndex].name,
                        selectedView
                    );
                    const tooltipContent = `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                                params.color
                            }; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${name}: </span>
                            <div>${impact < 1 ? " <1" : impact} ${this.getUnitTranslation(
                        this.selectedCriteria
                    )} </div>
                        </div>`;
                    return tooltipContent;
                },
            },
            legend: {
                top: "65%",
                bottom: "auto",
                selectedMode: false,
                formatter: (param: any) => {
                    return this.existingTranslation(param, selectedView);
                },
            },
            series: [
                {
                    type: "pie",
                    radius: ["50%", "90%"],
                    center: ["50%", "55%"],
                    // adjust the start angle
                    startAngle: 180,
                    label: {
                        show: true,
                        formatter: (param: any) => {
                            // correct the percentage
                            return `${this.existingTranslation(
                                param.name,
                                selectedView
                            )} ${(param.percent! * 2).toFixed(1)}%`;
                        },
                    },
                    data: echartsData,
                },
            ],
            color: Constants.COLOR,
        };
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
            "inventories-footprint.critere." + input + ".unite"
        );
    }
}
