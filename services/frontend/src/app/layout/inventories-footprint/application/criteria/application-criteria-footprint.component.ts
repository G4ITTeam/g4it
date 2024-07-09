/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NgxSpinnerService } from "ngx-spinner";
import { combineLatestWith, first, takeUntil } from "rxjs";
import { sortByProperty } from "sort-by-property";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import { FilterRepository } from "src/app/core/store/filter.repository";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
    ApplicationImpact,
    FootprintRepository,
    ImpactGraph,
} from "src/app/core/store/footprint.repository";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../abstract-dashboard";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
@Component({
    selector: "app-application-criteria-footprint.component",
    templateUrl: "./application-criteria-footprint.component.html",
})
export class ApplicationCriteriaFootprintComponent extends AbstractDashboard {
    selectedCriteria = {
        name: "",
        unite: "",
        title: "",
    };
    selectedCriteriaUri: string = "";
    selectedTranslatedCriteria: string = "";
    selectedInventoryId: number = 0;
    selectedUnit: string = "peopleeq";
    footprint: ApplicationFootprint[] = [];
    criteriaFootprint: ApplicationCriteriaFootprint[] = [];
    noData: boolean = false;
    selectedEnvironnementFilter: string[] = [];
    selectedLifecycleFilter: string[] = [];
    selectedEquipmentsFilter: string[] = [];
    selectedDomainFilter: string[] = [];
    selectedSubDomainFilter: string[] = [];
    domainFilter: string[] = [];
    selectedGraph: string = "global";
    selectedDomain: string = "";
    selectedSubdomain: string = "";
    selectedApp: string = "";
    impactOrder: ImpactGraph[] = [];
    yAxislist: string[] = [];

    options: EChartsOption = {};

    optionsLifecycle: EChartsOption = {};

    optionsEnv: EChartsOption = {};
    maxNumberOfBarsToBeDisplayed: number = 10;

    constructor(
        private spinner: NgxSpinnerService,
        private inventoryRepo: InventoryRepository,
        private appComponent: InventoriesApplicationFootprintComponent,
        override filterRepo: FilterRepository,
        override footprintRepo: FootprintRepository,
        override echartsRepo: EchartsRepository,
        override translate: TranslateService,
        private footprintService: FootprintService,
        private router: Router,
        override integerPipe: IntegerPipe,
        override decimalsPipe: DecimalsPipe,
    ) {
        super(
            filterRepo,
            footprintRepo,
            echartsRepo,
            translate,
            integerPipe,
            decimalsPipe,
        );
    }

    ngOnInit() {
        this.filterRepo.selectedApplicationFilters$
            .pipe(
                combineLatestWith(
                    this.footprintRepo.applicationFootprint$,
                    this.filterRepo.selectedCriteria$,
                    this.footprintRepo.appSelectedGraph$,
                    this.inventoryRepo.selectedInventory$,
                    this.footprintRepo.applicationCriteriaFootprint$,
                    this.footprintRepo.appSelectedDomain$,
                    this.footprintRepo.appSelectedSubdomain$,
                    this.footprintRepo.appSelectedApp$,
                ),
                takeUntil(this.ngUnsubscribe),
            )
            .subscribe(
                ([
                    selectedFilters,
                    applications,
                    criteria,
                    selectedGraph,
                    inventory,
                    criteriaFootprint,
                    selectedDomain,
                    selectedSubDomain,
                    selectedApp,
                ]) => {
                    this.selectedInventoryId = parseInt(inventory!);
                    this.criteriaFootprint =
                        this.appComponent.formatLifecycleCriteriaImpact(
                            criteriaFootprint,
                        );

                    this.footprint =
                        this.appComponent.formatLifecycleImpact(applications);

                    this.selectedApp = selectedApp;
                    this.selectedDomain = selectedDomain;
                    this.selectedSubdomain = selectedSubDomain;
                    this.selectedGraph = selectedGraph;
                    this.selectedEnvironnementFilter = selectedFilters.environments;
                    this.selectedEquipmentsFilter = selectedFilters.types;
                    this.selectedLifecycleFilter = this.appComponent.formatLifecycles(
                        selectedFilters.lifeCycles,
                    );
                    if (selectedFilters.subdomains === undefined) {
                        this.domainFilter = selectedFilters.domains;
                        this.initDomainFilter();
                    } else {
                        this.selectedDomainFilter = [];
                        this.selectedSubDomainFilter = [];
                        this.selectedDomainFilter = selectedFilters.domains;
                        this.selectedSubDomainFilter = selectedFilters.subdomains;
                    }

                    this.noData = this.checkIfNoData();

                    let doCallAppApi = false;
                    if (
                        this.selectedGraph === "application" &&
                        (this.criteriaFootprint.length === 0 ||
                            this.selectedCriteriaUri !== this.getCriteriaFromUrl())
                    ) {
                        doCallAppApi = true;
                    }

                    this.selectedCriteriaUri =
                        criteria === "" ? this.getCriteriaFromUrl() : criteria;
                    this.selectedCriteria = this.translate.instant(
                        `criteria.${this.selectedCriteriaUri}`,
                    );

                    if (doCallAppApi) {
                        this.CallAppApi();
                    } else {
                        this.options = this.loadBarChartOption();
                    }
                },
            );
    }

    private getCriteriaFromUrl(): string {
        const currentUrl = this.router.url;
        const segments = currentUrl.split("/");
        return segments[segments.length - 1];
    }

    initDomainFilter() {
        this.selectedDomainFilter = [];
        this.selectedSubDomainFilter = [];
        this.domainFilter.forEach((domain) => {
            const splitDomain = domain.split(",");
            this.selectedDomainFilter.push(splitDomain[0]);
            splitDomain.shift();
            if (splitDomain !== undefined) {
                splitDomain.forEach((subDomain) => {
                    this.selectedSubDomainFilter.push(subDomain);
                });
            }
        });
    }

    onChartClick(event: any) {
        if (this.selectedGraph === "global") {
            this.selectedGraph = "domain";
            this.footprintRepo.setSelectedGraph("domain");
            this.footprintRepo.setSelectedDomain(event.name);
            this.options = this.loadBarChartOption();
        } else if (this.selectedGraph === "domain") {
            this.selectedGraph = "subdomain";
            this.footprintRepo.setSelectedGraph("subdomain");
            this.footprintRepo.setSelectedSubdomain(event.name);
            this.options = this.loadBarChartOption();
        } else if (this.selectedGraph === "subdomain") {
            this.selectedGraph = "application";
            this.selectedApp = event.name;
            this.CallAppApi();
            this.footprintRepo.setSelectedGraph("application");
            this.footprintRepo.setSelectedApp(event.name);
        }
        this.footprintService
            .initFiltersApplication(this.selectedInventoryId)
            .pipe(first())
            .subscribe();
    }

    CallAppApi() {
        if (this.selectedApp === "") return;
        this.spinner.show();
        this.footprintService
            .initApplicationCriteriaFootprint(
                this.selectedInventoryId,
                this.selectedApp,
                this.selectedCriteriaUri,
            )
            .subscribe(() => {
                this.options = this.loadBarChartOption();
                this.spinner.hide();
            });
    }

    onArrowClick(graph: string) {
        if (graph === "application") {
            this.selectedGraph = "subdomain";
            this.selectedApp = "";
            this.footprintRepo.setSelectedGraph("subdomain");
            this.footprintRepo.setSelectedApp("");
            this.options = this.loadBarChartOption();
        } else if (graph === "subdomain") {
            this.selectedGraph = "domain";
            this.selectedSubdomain = "";
            this.footprintRepo.setSelectedGraph("domain");
            this.footprintRepo.setSelectedSubdomain("");
            this.options = this.loadBarChartOption();
        } else if (graph === "domain") {
            this.selectedGraph = "global";
            this.selectedDomain = "";
            this.footprintRepo.setSelectedGraph("global");
            this.footprintRepo.setSelectedDomain("");
            this.options = this.loadBarChartOption();
        }
        this.footprintService
            .initFiltersApplication(this.selectedInventoryId)
            .pipe(first())
            .subscribe();
    }

    checkIfNoData() {
        let hasNoData = true;
        this.footprint.forEach((criteria) => {
            criteria.impacts.forEach((impact: ApplicationImpact) => {
                if (
                    this.selectedEnvironnementFilter.includes(impact.environment) &&
                    this.selectedEquipmentsFilter.includes(impact.equipmentType) &&
                    this.selectedLifecycleFilter.includes(impact.lifeCycle) &&
                    this.selectedDomainFilter.includes(impact.domain) &&
                    this.selectedSubDomainFilter.includes(impact.subDomain)
                ) {
                    hasNoData = false;
                }
            });
        });
        return hasNoData;
    }

    computeData(barChartData: ApplicationFootprint[]) {
        let result: any = {};
        barChartData.forEach((data) => {
            if (data.criteria === this.selectedCriteriaUri) {
                data.impacts.forEach((impact: ApplicationImpact) => {
                    if (
                        this.selectedEnvironnementFilter.includes(impact.environment) &&
                        this.selectedEquipmentsFilter.includes(impact.equipmentType) &&
                        this.selectedLifecycleFilter.includes(impact.lifeCycle) &&
                        this.selectedDomainFilter.includes(impact.domain) &&
                        this.selectedSubDomainFilter.includes(impact.subDomain)
                    ) {
                        switch (this.selectedGraph) {
                            case "global":
                                this.computeImpactOrder(impact, impact.domain);
                                break;
                            case "domain":
                                if (impact.domain === this.selectedDomain) {
                                    this.computeImpactOrder(impact, impact.subDomain);
                                }
                                break;
                            case "subdomain":
                                if (
                                    impact.domain === this.selectedDomain &&
                                    impact.subDomain === this.selectedSubdomain
                                ) {
                                    this.computeImpactOrder(
                                        impact,
                                        impact.applicationName,
                                    );
                                }
                                break;
                        }
                    }
                });
            }
        });
        result = this.initGraphData(this.impactOrder);
        return result;
    }

    computeImpactOrder(impact: ApplicationImpact, yAxisValue: string) {
        if (!this.yAxislist.includes(yAxisValue)) {
            this.yAxislist.push(yAxisValue);
            this.impactOrder.push({
                domain: impact.domain,
                sipImpact: impact.sip,
                unitImpact: impact.impact,
                subdomain: impact.subDomain,
                app: impact.applicationName,
                equipment: impact.equipmentType,
                environnement: impact.environment,
                subdomains: [impact.subDomain],
                apps: [impact.applicationName],
                lifecycle: impact.lifeCycle,
            });
        } else {
            const index = this.yAxislist.indexOf(yAxisValue);
            this.impactOrder[index] = {
                domain: impact.domain,
                sipImpact: this.impactOrder[index].sipImpact + impact.sip,
                unitImpact: this.impactOrder[index].unitImpact + impact.impact,
                subdomain: impact.subDomain,
                app: impact.applicationName,
                equipment: impact.equipmentType,
                environnement: impact.environment,
                subdomains: this.impactOrder[index].subdomains.concat(impact.subDomain),
                apps: this.impactOrder[index].apps.concat(impact.applicationName),
                lifecycle: impact.lifeCycle,
            };
        }
    }

    initGraphData(impactOrder: any[]): any {
        impactOrder.sort(sortByProperty("sipImpact", "desc"));
        const xAxis: string[] = [];
        const yAxis: string[] = [];
        const unitImpact: number[] = [];
        const subdomainCount: number[] = [];
        const appCount: number[] = [];
        const equipmentList: string[] = [];
        const environnementList: string[] = [];
        impactOrder.forEach((impact) => {
            let subdomainList: string[] = [];
            let appList: string[] = [];
            switch (this.selectedGraph) {
                case "global":
                    xAxis.push(impact.domain);
                    yAxis.push(impact.sipImpact);
                    unitImpact.push(impact.unitImpact);
                    impact.subdomains.forEach((subdomain: string) => {
                        if (!subdomainList.includes(subdomain)) {
                            subdomainList.push(subdomain);
                        }
                    });
                    impact.apps.forEach((app: string) => {
                        if (!appList.includes(app)) {
                            appList.push(app);
                        }
                    });
                    subdomainCount.push(subdomainList.length);
                    appCount.push(appList.length);
                    break;
                case "domain":
                    xAxis.push(impact.subdomain);
                    yAxis.push(impact.sipImpact);
                    unitImpact.push(impact.unitImpact);
                    impact.apps.forEach((app: string) => {
                        if (!appList.includes(app)) {
                            appList.push(app);
                        }
                    });
                    appCount.push(appList.length);
                    break;
                case "subdomain":
                    xAxis.push(impact.app);
                    yAxis.push(impact.sipImpact);
                    unitImpact.push(impact.unitImpact);
                    break;
                case "application":
                    xAxis.push(impact.serverEnvironnementDuo);
                    yAxis.push(impact.sipImpact);
                    unitImpact.push(impact.unitImpact);
                    equipmentList.push(impact.equipment);
                    environnementList.push(impact.environnement);
                    break;
            }
        });
        return {
            xAxis,
            yAxis,
            unitImpact,
            subdomainCount,
            appCount,
            equipmentList,
            environnementList,
        };
    }

    computeAppData(barChartData: ApplicationCriteriaFootprint[]) {
        const vmAndEnvironnementList: string[] = [];
        const impactOrderCriteria: any[] = [];
        let vmAndEnvironnementName: string = "";
        const xAxis: string[] = [];
        const yAxis: string[] = [];
        const unitImpact: number[] = [];
        const clusterList: string[] = [];
        const equipmentList: string[] = [];
        const environnementList: string[] = [];
        barChartData[0].impacts.forEach((impact) => {
            if (
                this.selectedEnvironnementFilter.includes(impact.environment) &&
                this.selectedEquipmentsFilter.includes(impact.equipmentType) &&
                this.selectedLifecycleFilter.includes(impact.lifeCycle)
            ) {
                vmAndEnvironnementName = impact.vmName + "-" + impact.environment;
                if (!vmAndEnvironnementList.includes(vmAndEnvironnementName)) {
                    vmAndEnvironnementList.push(vmAndEnvironnementName);
                    impactOrderCriteria.push({
                        vmAndEnvironnementName: vmAndEnvironnementName,
                        sipImpact: impact.sip,
                        unitImpact: impact.impact,
                        equipment: impact.equipmentType,
                        environnement: impact.environment,
                        cluster: impact.cluster,
                    });
                } else {
                    const index = vmAndEnvironnementList.indexOf(vmAndEnvironnementName);
                    impactOrderCriteria[index] = {
                        vmAndEnvironnementName: vmAndEnvironnementName,
                        sipImpact: impactOrderCriteria[index].sipImpact + impact.sip,
                        unitImpact: impactOrderCriteria[index].unitImpact + impact.impact,
                        equipment: impact.equipmentType,
                        environnement: impact.environment,
                        cluster: impact.cluster,
                    };
                }
            }
        });
        impactOrderCriteria.sort(sortByProperty("sipImpact", "desc"));
        impactOrderCriteria.forEach((impact) => {
            xAxis.push(impact.vmAndEnvironnementName);
            yAxis.push(impact.sipImpact);
            unitImpact.push(impact.unitImpact);
            clusterList.push(impact.cluster);
            equipmentList.push(impact.equipment);
            environnementList.push(impact.environnement);
        });
        return {
            xAxis,
            yAxis,
            unitImpact,
            clusterList,
            equipmentList,
            environnementList,
        };
    }

    loadBarChartOption(): EChartsOption {
        const unit = this.selectedCriteria.unite;
        let result: any = {};
        this.impactOrder = [];
        this.yAxislist = [];
        let showZoom: boolean = true;
        if (this.selectedGraph === "application") {
            result = this.computeAppData(this.criteriaFootprint);
        } else {
            result = this.computeData(this.footprint);
        }
        if (result.yAxis.length < 10) {
            showZoom = false;
        }
        return {
            aria: {
                enabled: true,
                label: {
                    description: `${this.translate.instant(
                        "inventories-footprint.application.graph-critere",
                    )}`,
                },
            },
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    return `
                        <div>
                            <span style="font-weight: bold; margin-right: 15px;">${
                                result.xAxis[params.dataIndex]
                            } : </span>
                            <span>
                            Impact : ${this.integerPipe.transform(
                                result.yAxis[params.dataIndex],
                            )}
                                ${this.translate.instant("common.peopleeq-min")}
                            <br>
                            Impact : ${
                                result.unitImpact[params.dataIndex] < 1
                                    ? "< 1"
                                    : result.unitImpact[params.dataIndex].toFixed(0)
                            }
                            ${unit}
                            ${
                                this.selectedGraph === "global"
                                    ? "<br>" +
                                      this.translate.instant(
                                          "inventories-footprint.application.tooltip.nb-sd",
                                      ) +
                                      " : " +
                                      result.subdomainCount[params.dataIndex] +
                                      "<br>" +
                                      this.translate.instant(
                                          "inventories-footprint.application.tooltip.nb-app",
                                      ) +
                                      " : " +
                                      result.appCount[params.dataIndex]
                                    : ""
                            }
                            ${
                                this.selectedGraph === "domain"
                                    ? "<br>" +
                                      this.translate.instant(
                                          "inventories-footprint.application.tooltip.nb-app",
                                      ) +
                                      " : " +
                                      result.appCount[params.dataIndex]
                                    : ""
                            }
                            ${
                                this.selectedGraph === "application"
                                    ? "<br>" +
                                      this.translate.instant(
                                          "inventories-footprint.application.tooltip.cluster",
                                      ) +
                                      " : " +
                                      result.clusterList[params.dataIndex] +
                                      "<br>" +
                                      this.translate.instant(
                                          "inventories-footprint.application.tooltip.equipment",
                                      ) +
                                      " : " +
                                      result.equipmentList[params.dataIndex] +
                                      "<br>" +
                                      this.translate.instant(
                                          "inventories-footprint.application.tooltip.environnement",
                                      ) +
                                      " : " +
                                      result.environnementList[params.dataIndex]
                                    : ""
                            }
                            </span>
                        </div>
                    `;
                },
            },
            grid: {
                left: "3%",
                right: "4%",
                containLabel: true,
            },
            dataZoom: [
                {
                    show: showZoom,
                    startValue: result.xAxis[0],
                    endValue: result.xAxis[this.maxNumberOfBarsToBeDisplayed - 1],
                },
            ],
            xAxis: [
                {
                    type: "category",
                    data: result.xAxis,
                },
            ],
            yAxis: [
                {
                    type: "value",
                },
            ],
            series: [
                {
                    name: `${this.translate.instant(
                        "inventories-footprint.application.graph-cv",
                    )}`,
                    type: "bar",
                    data: result.yAxis,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }
}
