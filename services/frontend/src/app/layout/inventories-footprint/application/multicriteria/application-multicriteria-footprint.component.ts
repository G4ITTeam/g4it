/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { combineLatest, takeUntil } from "rxjs";
import { sortByProperty } from "sort-by-property";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import { FilterRepository } from "src/app/core/store/filter.repository";
import {
    ApplicationFootprint,
    FootprintRepository,
} from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";
import { AbstractDashboard } from "../../abstract-dashboard";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";

@Component({
    selector: "app-application-multicriteria-footprint.component",
    templateUrl: "./application-multicriteria-footprint.component.html",
})
export class ApplicationMulticriteriaFootprintComponent extends AbstractDashboard {
    selectedInventoryDate: string = "";
    footprint: ApplicationFootprint[] = [];
    noData: boolean = false;
    selectedEnvironnement: string[] = [];
    selectedLifecycle: string[] = [];
    selectedEquipments: string[] = [];
    selectedDomain: string[] = [];
    selectedSubDomain: string[] = [];
    domainFilter: string[] = [];

    options: EChartsOption = {};

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private appComponent: InventoriesApplicationFootprintComponent,
        override filterRepo: FilterRepository,
        override footprintRepo: FootprintRepository,
        override echartsRepo: EchartsRepository,
        override translate: TranslateService,
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

    ngOnInit(): void {
        combineLatest([
            this.filterRepo.selectedApplicationFilters$,
            this.footprintRepo.applicationFootprint$,
        ])
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe(([selectedFilters, applications]) => {
                this.footprintRepo.setSelectedApp("");
                this.footprintRepo.setSelectedDomain("");
                this.footprintRepo.setSelectedSubdomain("");
                this.footprintRepo.setSelectedGraph("global");
                this.footprint = this.appComponent.formatLifecycleImpact(applications);
                this.selectedEnvironnement = selectedFilters.environments;
                this.selectedEquipments = selectedFilters.types;
                this.selectedLifecycle = selectedFilters.lifeCycles;
                if (selectedFilters.subdomains === undefined) {
                    this.domainFilter = selectedFilters.domains;
                    this.initDomainFilter();
                } else {
                    this.selectedDomain = [];
                    this.selectedSubDomain = [];
                    this.selectedDomain = selectedFilters.domains;
                    this.selectedSubDomain = selectedFilters.subdomains;
                }
                if (this.checkIfNoData()) {
                    this.noData = true;
                } else {
                    this.noData = false;
                }
                this.options = this.loadBarChartOption(this.footprint);
            });
    }

    initDomainFilter() {
        this.selectedDomain = [];
        this.selectedSubDomain = [];
        this.domainFilter.forEach((domain) => {
            const splitDomain = domain.split(",");
            this.selectedDomain.push(splitDomain[0]);
            splitDomain.shift();
            if (splitDomain !== undefined) {
                splitDomain.forEach((subDomain) => {
                    this.selectedSubDomain.push(subDomain);
                });
            }
        });
    }

    onChartClick(event: any) {
        const translatedCriteria = this.translate.instant("criteria");

        const uri = this.getUriFromCriterias(translatedCriteria, event.name);

        if (uri) {
            this.router.navigate([`../${uri}`], {
                relativeTo: this.route,
            });
        }
    }

    getUriFromCriterias(translatedCriteria: any, criteria: string): string | undefined {
        let uri = undefined;
        for (const key in translatedCriteria) {
            if (translatedCriteria[key].title === criteria) {
                uri = key;
                break;
            }
        }
        return uri;
    }

    checkIfNoData() {
        let hasNoData = true;
        this.footprint.forEach((criteria) => {
            criteria.impacts.forEach((impact) => {
                if (
                    this.selectedEnvironnement.includes(impact.environment) &&
                    this.selectedEquipments.includes(impact.equipmentType) &&
                    this.selectedLifecycle.includes(impact.lifeCycle) &&
                    this.selectedDomain.includes(impact.domain) &&
                    this.selectedSubDomain.includes(impact.subDomain)
                ) {
                    hasNoData = false;
                }
            });
        });
        return hasNoData;
    }

    loadBarChartOption(barChartData: ApplicationFootprint[]): EChartsOption {
        const xAxis: any[] = [];
        const yAxis: any[] = [];
        const unitImpact: any[] = [];
        const unit: string[] = [];
        const impactOrder: any[] = [];
        barChartData.forEach((data) => {
            let sumSip = 0;
            let sumUnit = 0;
            data.impacts.forEach((impact) => {
                if (
                    this.selectedEnvironnement.includes(impact.environment) &&
                    this.selectedEquipments.includes(impact.equipmentType) &&
                    this.selectedLifecycle.includes(impact.lifeCycle) &&
                    this.selectedDomain.includes(impact.domain) &&
                    this.selectedSubDomain.includes(impact.subDomain)
                ) {
                    sumSip += impact.sip;
                    sumUnit += impact.impact;
                }
            });
            impactOrder.push({
                criteria: data.criteriaTitle,
                unite: data.unit,
                sipImpact: sumSip,
                unitImpact: sumUnit,
            });
        });
        impactOrder.sort(sortByProperty("sipImpact", "desc"));
        impactOrder.forEach((impact) => {
            xAxis.push(impact.criteria);
            yAxis.push(impact.sipImpact);
            unitImpact.push(impact.unitImpact);
            unit.push(impact.unite);
        });
        return {
            aria: {
                enabled: true,
                label: {
                    description: `${this.translate.instant(
                        "inventories-footprint.application.graph-global",
                    )}`,
                },
            },
            tooltip: {
                show: true,
                formatter: (params: any) => {
                    return `
                        <div style="display: flex; align-items: center; height: 30px;">
                            <span style="font-weight: bold; margin-right: 15px;">${
                                xAxis[params.dataIndex]
                            } : </span>
                            <div>
                            ${
                                unitImpact[params.dataIndex] < 1
                                    ? "< 1"
                                    : unitImpact[params.dataIndex].toFixed(0)
                            } 
                            ${unit[params.dataIndex]} </div>
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
                    name: `${this.translate.instant(
                        "inventories-footprint.application.graph-mc",
                    )}`,
                    type: "bar",
                    data: yAxis,
                },
            ],
            color: Constants.BLUE_COLOR,
        };
    }
}
