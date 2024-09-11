/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, Output, SimpleChanges } from "@angular/core";
import { EChartsOption } from "echarts";
import {
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
    ImpactACVStep,
    ImpactNetworkSipValue,
    ImpactSipValue,
    ServerImpact,
    ServersType,
    TerminalsImpact,
} from "src/app/core/interfaces/digital-service.interfaces";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { AbstractDashboard } from "src/app/layout/inventories-footprint/abstract-dashboard";
import { Constants } from "src/constants";

@Component({
    selector: "app-bar-chart",
    templateUrl: "./bar-chart.component.html",
})
export class BarChartComponent extends AbstractDashboard {
    @Input() barChartChild: boolean = false;
    @Input() selectedParam: string = "";
    @Input() selectedDetailParam: string = "";
    @Input() selectedDetailName: string = "";
    @Input() selectedCriteria: string = "acidification";

    @Input() networkData: DigitalServiceNetworksImpact[] = [];
    @Input() serverData: DigitalServiceServersImpact[] = [];
    @Input() terminalData: DigitalServiceTerminalsImpact[] = [];

    @Output() barChartChildChange: EventEmitter<any> = new EventEmitter();
    @Output() selectedDetailParamChange: EventEmitter<any> = new EventEmitter();
    @Output() selectedDetailNameChange: EventEmitter<any> = new EventEmitter();

    options: EChartsOption = {};

    terminalsOptions = [
        {
            key: "type",
            value: "type",
            name: this.translate.instant("digital-services.type"),
        },
        {
            key: "country",
            value: "country",
            name: this.translate.instant("digital-services.country"),
        },
    ];
    terminalsRadioButtonSelected: string = "type";
    serversOptions = [
        {
            key: "lifecycle",
            value: "lifecycle",
            name: this.translate.instant("digital-services.lifecycle"),
        },
        {
            key: "vm",
            value: "vm",
            name: this.translate.instant("digital-services.vm"),
        },
    ];
    serversRadioButtonSelected: string = "lifecycle";

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            if (this.selectedParam === "Network") {
                this.options = this.loadStackBarOptionNetwork(this.networkData);
            } else if (this.selectedParam === "Terminal") {
                this.options = this.loadStackBarOptionTerminal(this.terminalData);
            } else if (this.selectedParam === "Server" && !this.barChartChild) {
                this.options = this.loadStackBarOptionServer(this.serverData);
            } else if (this.selectedParam === "Server" && this.barChartChild) {
                this.options = this.loadStackBarOptionServerChild(this.serverData);
            }
        }
    }

    onChartClick(params: any) {
        if (
            (this.selectedParam == "Terminal" || this.selectedParam == "Server") &&
            !this.barChartChild
        ) {
            this.barChartChildChange.emit(true);
            this.selectedDetailNameChange.emit(params.seriesName);
            this.selectedDetailParamChange.emit(params.name);
        }
    }

    loadStackBarOptionNetwork(
        barChartData: DigitalServiceNetworksImpact[],
    ): EChartsOption {
        const seriesData = this.getSelectedCriteriaData(barChartData, "impacts");
        const xAxis: any[] = [];
        const yAxis: any[] = [];

        seriesData.forEach((impact: ImpactNetworkSipValue) => {
            xAxis.push(impact.networkType);
            yAxis.push({
                value: impact.sipValue < 1 ? impact.sipValue : impact.sipValue.toFixed(0),
                rawValue: impact.rawValue,
                unit: impact.unit,
            });
        });
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    return `
                        <div>
                            ${this.integerPipe.transform(params.value)}
                            ${this.translate.instant("common.peopleeq-min")}<br>
                            ${this.decimalsPipe.transform(params.data.rawValue)} ${params.data.unit}  
                        </div>
                    `;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                },
            ],
            yAxis: [
                {
                    type: "value",
                },
            ],
            series: [
                {
                    name: "networks",
                    type: "bar",
                    data: yAxis,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }

    loadStackBarOptionTerminal(barChartData: any): EChartsOption {
        let seriesData: any[] = [];
        const xAxis: any[] = [];
        const yAxis: any[] = [];

        // Extract the selected data based on radio button
        if (this.terminalsRadioButtonSelected === "type") {
            seriesData = this.getSelectedCriteriaData(barChartData, "impactType");
        } else if (this.terminalsRadioButtonSelected === "country") {
            seriesData = this.getSelectedCriteriaData(barChartData, "impactCountry");
        }

        // Process the data based on whether it's a child chart or not
        this.processParentOrChildTerminalsData(seriesData, xAxis, yAxis);

        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const showedHtml = `
                            <div> 
                                ${this.integerPipe.transform(params.value)}
                                ${this.translate.instant("common.peopleeq-min")} <br>
                                ${this.decimalsPipe.transform(params.data.rawValue)} ${params.data.unit}
                            </div>
                            `;

                    let otherHtml = "";

                    if (!this.barChartChild) {
                        otherHtml = `
                            <div>
                                ${this.translate.instant(
                                    "digital-services-terminals.nb-user",
                                )}: ${this.decimalsPipe.transform(params.data.nbUsers)}
                            </div>
                            <div>
                                ${this.translate.instant(
                                    "digital-services-terminals.yearly-usage",
                                )}: ${this.decimalsPipe.transform(params.data.usageTime)}
                                ${this.translate.instant("digital-services-terminals.hours")}
                            </div>
                      `;
                    }

                    return showedHtml + otherHtml;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                },
            ],
            yAxis: [
                {
                    type: "value",
                },
            ],
            series: [
                {
                    name: "terminals",
                    type: "bar",
                    data: yAxis,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }

    getSelectedCriteriaData(barChartData: any, key: string): any[] {
        const selectedData = barChartData.find(
            (impact: any) => impact.criteria === this.selectedCriteria,
        );
        return selectedData ? selectedData[key] : [];
    }

    processParentOrChildTerminalsData(seriesData: any[], xAxis: any[], yAxis: any[]) {
        if (!this.barChartChild) {
            seriesData.forEach((impact: TerminalsImpact) => {
                xAxis.push(impact.name);
                yAxis.push({
                    value:
                        impact.totalSipValue < 1
                            ? impact.totalSipValue
                            : impact.totalSipValue.toFixed(0),
                    name: impact.name,
                    nbUsers: impact.totalNbUsers,
                    usageTime: impact.avgUsageTime,
                    rawValue: impact.rawValue,
                    unit: impact.unit,
                });
            });
        } else {
            const childData = seriesData.find(
                (item: any) => item.name === this.selectedDetailParam,
            );

            if (childData) {
                const order = LifeCycleUtils.getLifeCycleList();

                childData.impact.forEach((impact: any) => {
                    impact.ACVStep =
                        LifeCycleUtils.getLifeCycleMap().get(impact.ACVStep) ||
                        impact.ACVStep;
                });

                childData.impact.sort((a: any, b: any) => {
                    return order.indexOf(a.ACVStep) - order.indexOf(b.ACVStep);
                });

                childData.impact.forEach((impact: any) => {
                    xAxis.push(this.existingTranslation(impact.ACVStep, "acvStep"));
                    yAxis.push({
                        value: impact.sipValue,
                        name: impact.name,
                        nbUsers: impact.totalNbUsers,
                        usageTime: impact.avgUsageTime,
                        rawValue: impact.rawValue,
                        unit: impact.unit,
                    });
                });
            }
        }
    }

    loadStackBarOptionServer(barChartData: any): EChartsOption {
        const xAxisData: string[] = [];
        const seriesData: any[] = [];
        let detailServers: any[] = [];

        const data4Criteria = this.getSelectedCriteriaData(barChartData, "impactsServer");

        data4Criteria.forEach((impact: ServersType) => {
            let serverType = `digital-services-servers.server-type.${impact.mutualizationType}-${impact.serverType}`;
            xAxisData.push(serverType);

            impact.servers.forEach((server: ServerImpact, index: any) => {
                detailServers.push({
                    name: server.name,
                    vmList: server.impactVmDisk,
                    hostingEfficiency: server.hostingEfficiency,
                });
                seriesData.push({
                    name: server.name,
                    data: [
                        [
                            serverType,
                            server.totalSipValue < 1
                                ? server.totalSipValue
                                : server.totalSipValue.toFixed(0),
                            server.impactVmDisk[0].rawValue,
                            server.impactVmDisk[0].unit,
                        ],
                    ],
                    type: "bar",
                    stack: "Ad",
                    emphasis: {
                        focus: "series",
                    },
                    itemStyle: {
                        color: this.createStackBarGradientColor(
                            index,
                            impact.servers.length,
                        ),
                    },
                });
            });
        });
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    let hostingEfficiency =
                        detailServers.find(
                            (server: any) => server.name === params.seriesName,
                        ).hostingEfficiency || "N/A";

                    const vmNameNbList = detailServers
                        .find((server: any) => server.name === params.seriesName)
                        .vmList.map((vm: any, index: any) => {
                            const str = `${vm.name} (${vm.quantity}) ; `;
                            return (index + 1) % 3 === 0 ? str + "<br/>" : str;
                        })
                        .join("");
                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="display: inline-block; width: 10px; height: 10px; background-color: ${params.color}; border-radius: 50%; margin-right: 5px;"></span>
                            <span style="font-weight: bold; margin-right: 15px;">${params.seriesName}</span>
                        </div>
                        <div>
                            ${vmNameNbList}
                        </div>
                        <div>Impact: ${this.integerPipe.transform(params.data[1])}
                            ${this.translate.instant("common.peopleeq-min")} <br>
                            ${this.decimalsPipe.transform(params.data[2])} ${params.data[3]} 
                        </div>
                        <div>
                            ${this.translate.instant(
                                "digital-services-servers.hosting-efficiency",
                            )}: ${this.translate.instant(
                                "digital-services-servers." + hostingEfficiency,
                            )}
                        </div>`;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: {
                type: "category",
                data: xAxisData,
                axisLabel: {
                    formatter: (value) => this.translate.instant(value) || value,
                    interval: 0, // Display all labels
                },
            },
            yAxis: {
                type: "value",
            },
            series: seriesData,
        };
    }

    loadStackBarOptionServerChild(barChartData: any): EChartsOption {
        const xAxis: any[] = [];
        const seriesData: any[] = [];

        const selectedServer =
            this.getSelectedCriteriaData(barChartData, "impactsServer")
                .find(
                    (data: ServersType) =>
                        `digital-services-servers.server-type.${data.mutualizationType}-${data.serverType}` ===
                        this.selectedDetailParam,
                )
                .servers.find(
                    (data: ServerImpact) => data.name === this.selectedDetailName,
                ) || null;

        if (this.serversRadioButtonSelected === "lifecycle") {
            const order = LifeCycleUtils.getLifeCycleList();
            const lifecycleMap = LifeCycleUtils.getLifeCycleMap();

            selectedServer.impactStep.forEach(
                (impact: any) =>
                    (impact.acvStep = lifecycleMap.get(impact.acvStep) || impact.acvStep),
            );

            selectedServer.impactStep.sort((a: any, b: any) => {
                return order.indexOf(a.acvStep) - order.indexOf(b.acvStep);
            });

            selectedServer.impactStep.forEach((lifecycle: ImpactACVStep) => {
                xAxis.push(`acvStep.${lifecycle.acvStep}`);
                seriesData.push({
                    value: lifecycle.sipValue,
                    rawValue: lifecycle.rawValue,
                    unit: lifecycle.unit,
                });
            });
        } else if (this.serversRadioButtonSelected === "vm") {
            selectedServer.impactVmDisk.sort((a: any, b: any) =>
                a.name.localeCompare(b.name),
            );
            selectedServer.impactVmDisk.forEach((vm: ImpactSipValue) => {
                xAxis.push(vm.name);
                seriesData.push({
                    value: vm.sipValue < 1 ? vm.sipValue : vm.sipValue.toFixed(0),
                    rawValue: vm.rawValue,
                    unit: vm.unit,
                    quantity: vm.quantity,
                });
            });
        }
        return {
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    const showedHtml = `
                        <div> 
                            ${this.integerPipe.transform(params.value)}
                            ${this.translate.instant("common.peopleeq-min")}<br>
                            ${this.decimalsPipe.transform(params.data.rawValue)} ${params.data.unit} 
                        </div>
                    `;

                    let showedVmHtml = "";

                    if (this.serversRadioButtonSelected === "vm") {
                        showedVmHtml = `
                            <div>
                                ${this.translate.instant(
                                    "digital-services-servers.quantity",
                                )}: ${params.data.quantity} ${this.translate.instant(
                                    "digital-services-servers.vms",
                                )}
                            </div>
                        `;
                    }

                    return showedVmHtml + showedHtml;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                bottom: "3%",
                containLabel: true,
            },
            xAxis: [
                {
                    type: "category",
                    data: xAxis,
                    axisLabel: {
                        formatter: (value) => this.translate.instant(value) || value,
                        interval: 0, // Display all labels
                    },
                },
            ],
            yAxis: [
                {
                    type: "value",
                },
            ],
            series: [
                {
                    name: "servers",
                    type: "bar",
                    data: seriesData,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }

    changeTerminalsRadioButtonSelected() {
        this.options = this.loadStackBarOptionTerminal(this.terminalData);
    }

    changeServersRadioButtonSelected() {
        this.options = this.loadStackBarOptionServerChild(this.serverData);
    }

    createStackBarGradientColor(index: number, totalCount: number): any {
        if (totalCount == 1) {
            return Constants.BLUE_COLOR;
        }
        const startColor = Constants.BLUE_COLOR;
        const endColor = Constants.YELLOW_COLOR;
        const t = index / (totalCount - 1);
        const startR = parseInt(startColor.slice(1, 3), 16);
        const startG = parseInt(startColor.slice(3, 5), 16);
        const startB = parseInt(startColor.slice(5, 7), 16);
        const endR = parseInt(endColor.slice(1, 3), 16);
        const endG = parseInt(endColor.slice(3, 5), 16);
        const endB = parseInt(endColor.slice(5, 7), 16);
        const r = Math.round((1 - t) * startR + t * endR);
        const g = Math.round((1 - t) * startG + t * endG);
        const b = Math.round((1 - t) * startB + t * endB);
        return `rgb(${r},${g},${b})`;
    }
}
