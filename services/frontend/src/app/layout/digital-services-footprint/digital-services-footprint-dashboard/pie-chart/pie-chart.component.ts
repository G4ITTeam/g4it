/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, Output, SimpleChanges } from "@angular/core";
import { EChartsOption } from "echarts";
import { DigitalServiceFootprint } from "src/app/core/interfaces/digital-service.interfaces";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
@Component({
    selector: "app-pie-chart",
    templateUrl: "./pie-chart.component.html",
})
export class PieChartComponent extends AbstractDashboard {
    @Input() globalVisionChartData: DigitalServiceFootprint[] | undefined;
    @Input() selectedCriteria: string = "acidification";
    @Output() selectedParamChange: EventEmitter<any> = new EventEmitter();
    @Output() chartTypeChange: EventEmitter<any> = new EventEmitter();

    options: EChartsOption = {};

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.options = this.loadPieChartOption(this.globalVisionChartData || []);
        }
    }

    onChartClick(params: any) {
        this.chartTypeChange.emit("bar");
        this.selectedParamChange.emit(params.data.tier);
    }

    loadPieChartOption(chartData: any[]): EChartsOption {
        const order = ["Terminal", "Network", "Server"];
        const seriesData = chartData.map((item) => {
            const selectedImpact = item.impacts.find(
                (impact: any) =>
                    impact.criteria.split(" ").slice(0, 2).join(" ") ===
                    this.selectedCriteria,
            );
            const value = selectedImpact ? selectedImpact.sipValue : 0;
            return {
                name: this.existingTranslation(item.tier, "digital-services"),
                value: value,
                tier: item.tier,
            };
        });
        seriesData.sort((a: any, b: any) => {
            return order.indexOf(a.tier) - order.indexOf(b.tier);
        });
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const percentage = params.percent.toFixed(0);
                    const name = this.existingTranslation(
                        params.name,
                        "digital-services",
                    );
                    return `
                    <div style="display: flex; align-items: center; height: 30px;">
                        <span style="display: inline-block; width: 10px; height: 10px; background-color: ${
                            params.color
                        }; border-radius: 50%; margin-right: 5px;"></span>
                        <span style="font-weight: bold; margin-right: 15px;">${name}</span>
                    </div>
                    <div>${percentage} %</div>
                    <div>${this.integerPipe.transform(params.value)}
                    ${this.translate.instant("common.peopleeq-min")} </div>
                    `;
                },
            },
            legend: {
                orient: "horizontal",
                formatter: (param: any) => {
                    return this.existingTranslation(param, "digital-services");
                },
            },
            series: [
                {
                    name: "Access From",
                    type: "pie",
                    radius: "70%",
                    data: seriesData,
                    emphasis: {
                        itemStyle: {
                            shadowBlur: 10,
                            shadowOffsetX: 0,
                            shadowColor: "rgba(0, 0, 0, 0.5)",
                        },
                    },
                },
            ],
            color: Constants.COLOR,
        };
    }
}
