/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, Input, SimpleChanges } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { Subject, takeUntil } from "rxjs";
import {
    ApplicationCriteriaFootprint,
    ApplicationCriteriaImpact,
    ApplicationFootprint,
    ApplicationImpact,
    FootprintRepository,
} from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";

@Component({
    selector: "app-application-criteria-pie-chart",
    templateUrl: "./application-criteria-pie-chart.component.html",
})
export class ApplicationCriteriaPieChartComponent {
    @Input() selectedGraph: string = "global";
    isSip: boolean = true;
    @Input() selectedCriteria = {
        name: "",
        unite: "",
    };
    @Input() selectedCriteriaUri: string = "";
    @Input() footprint: ApplicationFootprint[] = [];
    criteriaFootprint: ApplicationCriteriaFootprint[] = [];
    noData: boolean = false;
    @Input() selectedEnvironnementFilter: string[] = [];
    @Input() selectedLifecycleFilter: string[] = [];
    @Input() selectedEquipmentsFilter: string[] = [];
    @Input() selectedDomainFilter: string[] = [];
    @Input() selectedSubDomainFilter: string[] = [];
    @Input() selectedApp: string = "";
    @Input() selectedSubdomain: string = "";
    @Input() selectedDomain: string = "";
    lifecycleOptions: EChartsOption = {};
    envOptions: EChartsOption = {};

    ngUnsubscribe = new Subject<void>();

    constructor(
        private footprintRepo: FootprintRepository,
        private translate: TranslateService,
        private appComponent: InventoriesApplicationFootprintComponent
    ) {}

    ngOnInit() {
        this.footprintRepo.applicationCriteriaFootprint$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((criteriaFootprint) => {
                this.criteriaFootprint =
                    this.appComponent.formatLifecycleCriteriaImpact(criteriaFootprint);
                this.initLifecycleGraph();
            });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            this.initLifecycleGraph();
            if (this.selectedGraph === "application") {
                this.initEnvGraph();
            }
        }
    }

    initLifecycleGraph() {
        const data: any[] = [];
        const lifecyles: string[] = [];
        this.footprint.forEach((application) => {
            if (application.criteria === this.selectedCriteriaUri) {
                application.impacts.forEach((impact: ApplicationImpact) => {
                    if (
                        this.selectedEnvironnementFilter.includes(impact.environment) &&
                        this.selectedEquipmentsFilter.includes(impact.equipmentType) &&
                        this.selectedLifecycleFilter.includes(impact.lifeCycle) &&
                        this.selectedDomainFilter.includes(impact.domain) &&
                        this.selectedSubDomainFilter.includes(impact.subDomain)
                    ) {
                        switch (this.selectedGraph) {
                            case "global":
                                if (!lifecyles.includes(impact.lifeCycle)) {
                                    lifecyles.push(impact.lifeCycle);
                                    data.push({
                                        name: impact.lifeCycle,
                                        value: impact.sip,
                                    });
                                } else {
                                    const index = lifecyles.indexOf(impact.lifeCycle);
                                    data[index].value += impact.sip;
                                }
                                break;
                            case "domain":
                                if (this.selectedDomain === impact.domain) {
                                    if (!lifecyles.includes(impact.lifeCycle)) {
                                        lifecyles.push(impact.lifeCycle);
                                        data.push({
                                            name: impact.lifeCycle,
                                            value: impact.sip,
                                        });
                                    } else {
                                        const index = lifecyles.indexOf(impact.lifeCycle);
                                        data[index].value += impact.sip;
                                    }
                                }
                                break;
                            case "subdomain":
                                if (
                                    this.selectedDomain === impact.domain &&
                                    this.selectedSubdomain === impact.subDomain
                                ) {
                                    if (!lifecyles.includes(impact.lifeCycle)) {
                                        lifecyles.push(impact.lifeCycle);
                                        data.push({
                                            name: impact.lifeCycle,
                                            value: impact.sip,
                                        });
                                    } else {
                                        const index = lifecyles.indexOf(impact.lifeCycle);
                                        data[index].value += impact.sip;
                                    }
                                }
                                break;
                            case "application":
                                if (
                                    this.selectedDomain === impact.domain &&
                                    this.selectedSubdomain === impact.subDomain &&
                                    this.selectedApp === impact.applicationName
                                ) {
                                    if (!lifecyles.includes(impact.lifeCycle)) {
                                        lifecyles.push(impact.lifeCycle);
                                        data.push({
                                            name: impact.lifeCycle,
                                            value: impact.sip,
                                        });
                                    } else {
                                        const index = lifecyles.indexOf(impact.lifeCycle);
                                        data[index].value += impact.sip;
                                    }
                                }
                                break;
                        }
                    }
                });
            }
        });
        this.lifecycleOptions = {
            aria: {
                enabled: true,
                label: {
                    description: `${this.translate.instant(
                        "inventories-footprint.application.graph-lifecycle"
                    )}`,
                },
            },
            series: [
                {
                    type: "pie",
                    data: data,
                    radius: "70%",
                    color: Constants.GREEN_COLOR_SET,
                    label: {
                        show: true,
                        formatter: function (params) {
                            return `${params.name}\n${params.percent!.toFixed(1)}%`;
                        },
                    },
                },
            ],
        };
    }

    initEnvGraph() {
        const data: any[] = [];
        const environments: string[] = [];
        this.criteriaFootprint.forEach((application) => {
            if (application.criteria === this.selectedCriteriaUri) {
                application.impacts.forEach((impact: ApplicationCriteriaImpact) => {
                    if (
                        this.selectedEnvironnementFilter.includes(impact.environment) &&
                        this.selectedEquipmentsFilter.includes(impact.equipmentType) &&
                        this.selectedLifecycleFilter.includes(impact.lifeCycle)
                    ) {
                        if (!environments.includes(impact.environment)) {
                            environments.push(impact.environment);
                            data.push({
                                name: impact.environment,
                                value: impact.sip,
                            });
                        } else {
                            const index = environments.indexOf(impact.environment);
                            data[index].value += impact.sip;
                        }
                    }
                });
            }
        });
        this.envOptions = {
            aria: {
                enabled: true,
                label: {
                    description: `${this.translate.instant(
                        "inventories-footprint.application.graph-env"
                    )}`,
                },
            },
            series: [
                {
                    type: "pie",
                    data: data,
                    radius: "70%",
                    color: Constants.PURPLE_COLOR_SET,
                    label: {
                        show: true,
                        formatter: function (params) {
                            return `${params.name}\n${params.percent!.toFixed(1)}%`;
                        },
                    },
                },
            ],
        };
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
