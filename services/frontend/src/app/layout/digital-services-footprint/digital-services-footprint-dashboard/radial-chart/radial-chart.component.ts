/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, Input, SimpleChanges } from "@angular/core";
import { EChartsOption } from "echarts";
import { DigitalServiceFootprint } from "src/app/core/interfaces/digital-service.interfaces";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";

@Component({
    selector: "app-radial-chart",
    templateUrl: "./radial-chart.component.html",
})
export class RadialChartComponent extends AbstractDashboard {
    @Input() globalVisionChartData: DigitalServiceFootprint[] | undefined;

    options: EChartsOption = {};

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.options = this.loadRadialChartOption(this.globalVisionChartData || []);
        }
    }

    loadRadialChartOption(radialChartData: DigitalServiceFootprint[]): EChartsOption {
        const order = ["Terminal", "Network", "Server"];
        radialChartData.sort((a: any, b: any) => {
            return order.indexOf(a.tier) - order.indexOf(b.tier);
        });
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const dataIndex = params.dataIndex;
                    const seriesIndex = params.seriesIndex;
                    const impact = radialChartData[seriesIndex].impacts[dataIndex];
                    const roundedImpact = impact.sipValue.toFixed(0);
                    const name = this.existingTranslation(
                        radialChartData[seriesIndex].tier,
                        "digital-services"
                    );
                    return `<div style="display: flex; align-items: center; height: 30px;">
                    <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                        params.color
                    }; border-radius: 50%; margin-right: 5px;"></span>
                    <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                </div>
                    <div>${this.getCriteriaTranslation(
                        impact.criteria.split(" ").slice(0, 2).join(" ")
                    )} : ${
                        impact.sipValue < 1 ? " < 1" : roundedImpact
                    } ${this.translate.instant("common.peopleeq-min")} </div>
                </div>
            `;
                },
            },
            angleAxis: {
                type: "category",
                data: radialChartData[0].impacts.map((impact: any) => {
                    const twoWordsImpact = impact.criteria
                        .split(" ")
                        .slice(0, 2)
                        .join(" ");
                    return this.getCriteriaTranslation(twoWordsImpact);
                }),
            },
            radiusAxis: {
                name: this.translate.instant("common.peopleeq"),
                nameLocation: "end",
                nameTextStyle: {
                    fontStyle: "italic",
                },
            },
            polar: {
                radius: "80%",
                center: ["50%", "55%"],
            },
            series: radialChartData.map((item: any) => ({
                name: item.tier,
                type: "bar",
                coordinateSystem: "polar",
                data: item.impacts.map((impact: any) => ({
                    value: impact.sipValue,
                    label: {
                        formatter: (params: any) => {
                            return `${impact.unitValue} ${impact.unit}`;
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
                data: radialChartData.map((item: any) => item.tier),
                formatter: (param: any) => {
                    return this.existingTranslation(param, "digital-services");
                },
            },
            color: Constants.COLOR,
        };
    }
}
