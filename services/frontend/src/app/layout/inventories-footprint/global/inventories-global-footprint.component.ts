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
import { ChartData, ComputedSelection } from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../abstract-dashboard";

@Component({
    selector: "app-inventories-global-footprint",
    templateUrl: "./inventories-global-footprint.component.html",
})
export class InventoriesGlobalFootprintComponent
    extends AbstractDashboard
    implements OnInit
{
    noData = true;
    options: EChartsOption = {};

    chartData: ChartData<ComputedSelection> = {};

    ngOnInit(): void {
        combineLatest([this.echartsRepo.mainChart$, this.filterRepo.selectedView$])
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe(([chartData, selectedView]) => {
                this.noData = chartData.length === 0;
                if (this.noData) {
                    return;
                }
                this.updateEchartsOptions(selectedView, chartData);
            });
    }

    updateEchartsOptions(selectedView: string, echartsData: any) {
        this.options = {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    const seriesIndex = params.seriesIndex;
                    const impact = echartsData[seriesIndex].impacts[dataIndex];
                    const roundedImpact = impact.fis.toFixed(0);
                    const name = this.existingTranslation(
                        echartsData[seriesIndex].data,
                        selectedView
                    );
                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                                params.color
                            }; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                            <div>${impact.critere} : ${
                        impact.sipValue < 1 ? " < 1" : roundedImpact
                    } ${this.translate.instant("common.peopleeq-min")} </div>
                        </div>
                    `;
                },
            },
            angleAxis: {
                type: "category",
                data: echartsData[0].impacts.map((impact: any) => impact.critere),
            },
            radiusAxis: {
                name: this.translate.instant("common.peopleeq"),
                nameLocation: "end",
                nameTextStyle: {
                    fontStyle: "italic",
                },
            },
            polar: {
                radius: "80%", //TODO Adjust the radius value to add margin for legend if lot of data, have to be set dynamically
                center: ["50%", "55%"],
            },
            series: echartsData.map((item: any) => ({
                name: item.data,
                type: "bar",
                coordinateSystem: "polar",
                data: item.impacts.map((impact: any) => ({
                    value: impact.fis,
                    label: {
                        formatter: (params: any) => {
                            return `${impact.impactUnitaire} ${impact.unite}`;
                        },
                    },
                })),
                stack: "a",
                emphasis: {
                    focus: "series",
                },
            })),
            legend: {
                show: true,
                data: echartsData.map((item: any) => item.data),
                formatter: (param: any) => {
                    return this.existingTranslation(param, selectedView);
                },
            },
            color: Constants.COLOR,
        };
    }
}
