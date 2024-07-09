/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MenuItem } from "primeng/api";
import { Subject, combineLatest, takeUntil, tap } from "rxjs";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import { Filter, FilterRepository } from "src/app/core/store/filter.repository";
import {
    ChartData,
    ComputedSelection,
    FootprintRepository,
} from "src/app/core/store/footprint.repository";
import * as UtilsCriteria from "src/app/core/utils/criteria";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";

@Component({
    selector: "app-inventories-footprint",
    templateUrl: "./inventories-footprint.component.html",
})
export class InventoriesFootprintComponent implements OnInit {
    selectedView: string = "";

    selectedCriteria: string = "";

    echartsData: any = [];

    chartData: ChartData<ComputedSelection> = {};

    criteres: MenuItem[] = [
        "multi-criteria",
        ...UtilsCriteria.getCriteriaShortList(),
    ].map((criteria) => {
        return {
            label: this.translate.instant(`criteria.${criteria}.title`),
            routerLink: criteria,
        };
    });

    order = LifeCycleUtils.getLifeCycleList();
    lifeCycleMap = LifeCycleUtils.getLifeCycleMap();

    ngUnsubscribe = new Subject<void>();
    inventoryId = 0;

    constructor(
        private activatedRoute: ActivatedRoute,
        public filterRepo: FilterRepository,
        public footprintRepo: FootprintRepository,
        public footprintService: FootprintService,
        public echartsRepo: EchartsRepository,
        private router: Router,
        private spinner: NgxSpinnerService,
        private translate: TranslateService,
    ) {}

    async ngOnInit(): Promise<void> {
        // Loader is present until the very first charts have been initialized
        // Will reappear only if the inventories-footprint component is destroyed (on page reload)
        this.echartsRepo.setIsDataInitialized(false);
        this.spinner.show();
        this.echartsRepo.isDataInitialized$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((chartInitialized: boolean) => {
                if (chartInitialized) {
                    this.spinner.hide();
                }
            });

        // Set active inventory based on route
        const selectedInventoryId: number = this.activatedRoute.snapshot.paramMap.get(
            "inventoryId",
        )
            ? parseInt(this.activatedRoute.snapshot.paramMap.get("inventoryId")!)
            : 0;

        this.inventoryId = selectedInventoryId;

        //Set footprint with associated filters and retrieve datacenter and physical equipement datas
        this.footprintService.retrieveFootprint(
            selectedInventoryId,
            this.getCriteriaFromUrl(),
            "equipment",
        );

        this.subscribeToComputedData();

        this.filterRepo.selectedCriteria$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((criteria: string) => {
                if (criteria) {
                    this.selectedCriteria = criteria;
                    this.updateCharts();
                }
            });

        this.filterRepo.selectedView$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((view: string) => {
                this.selectedView = view;
                this.updateCharts();
            });

        this.filterRepo.selectedFilters$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((filters: Filter) => {
                this.footprintService.refreshActiveImpacts(filters);
                const criteria = this.getCriteriaFromUrl();
                const unite = this.getUniteFromCriteria(criteria);

                this.footprintService.updateSelectedCriteria(criteria, unite);
            });

        this.router.events.subscribe((event) => {
            if (event instanceof NavigationEnd) {
                const criteria = this.getCriteriaFromUrl();
                const unite = this.getUniteFromCriteria(criteria);

                this.footprintService.updateSelectedCriteria(criteria, unite);
            }
        });
    }

    private getCriteriaFromUrl(): string {
        const currentUrl = this.router.url;
        const segments = currentUrl.split("/");
        return segments[segments.length - 1];
    }

    private getUniteFromCriteria(criteria: string): string {
        return this.translate.instant(`criteria.${criteria}.unite`);
    }

    subscribeToComputedData() {
        combineLatest([
            this.footprintRepo.particuleComputedSelection$,
            this.footprintRepo.radiationComputedSelection$,
            this.footprintRepo.acidificationComputedSelection$,
            this.footprintRepo.resourceComputedSelection$,
            this.footprintRepo.climateComputedSelection$,
        ])
            .pipe(
                tap(([particule, radiation, acidification, resource, climate]) => {
                    // same order as criteria.ts
                    this.chartData = {
                        "climate-change": this.translateLifeCycle(climate),
                        "resource-use": this.translateLifeCycle(resource),
                        "ionising-radiation": this.translateLifeCycle(radiation),
                        acidification: this.translateLifeCycle(acidification),
                        "particulate-matter": this.translateLifeCycle(particule),
                    };
                }),
            )
            .subscribe(() => this.updateCharts());
    }

    translateLifeCycle(data: ComputedSelection): ComputedSelection {
        if (data.acvStep.length == 0) return data;
        data.acvStep.forEach((step) => {
            const acvStepTranslated = this.lifeCycleMap.get(step.name);
            if (acvStepTranslated) step.name = acvStepTranslated;
        });
        return data;
    }

    updateCharts() {
        if (!this.selectedCriteria) return;
        if (this.selectedCriteria === "multi-criteria") {
            this.updateMainChartData();
        } else {
            this.updateDonutChartData();
        }
    }

    updateDonutChartData() {
        if (this.selectedCriteria === "" || this.selectedView === "") {
            this.echartsRepo.setCritereChart([]);
            return;
        }
        const echartsData = [];
        const otherData: any = [];

        const selectedChartData =
            this.chartData[this.selectedCriteria as keyof ChartData<ComputedSelection>][
                this.selectedView as keyof ComputedSelection
            ];

        const total = selectedChartData
            .map((data) => data.impact || 0)
            .reduce((sum, current) => sum + current, 0);

        for (let data of selectedChartData) {
            const v: any = { value: data.impact, name: data.name };

            var percent = (data.impact / total) * 100;
            if (percent < 1) v.percent = percent;
            percent < 1 ? otherData.push(v) : echartsData.push(v);
        }

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

        if (this.selectedView == "acvStep") {
            echartsData.sort((a: any, b: any) => {
                return this.order.indexOf(a.name) - this.order.indexOf(b.name);
            });
        } else {
            // Sort by alphabetical order
            echartsData.sort((a: any, b: any) => a.name.localeCompare(b.name));
        }

        this.echartsRepo.setCritereChart(echartsData);
    }

    updateMainChartData() {
        if (this.selectedView === "") {
            this.echartsRepo.setMainChart([]);
            return;
        }
        const echartsData: any[] = [];
        for (let prop in this.chartData) {
            const { unite, title } = this.translate.instant(`criteria.${prop}`);
            for (let data of this.chartData[prop as keyof ChartData<ComputedSelection>][
                this.selectedView as keyof ComputedSelection
            ]) {
                const impact = {
                    critere: title,
                    fis: data.sip,
                    impactUnitaire: data.impact,
                    unite,
                };
                const view = {
                    data: data.name,
                    impacts: [impact],
                };

                const viewExist = echartsData.find(
                    (data: any) => data.data === view.data,
                );
                if (viewExist) {
                    viewExist.impacts.push(impact);
                } else {
                    echartsData.push(view);
                }
            }
        }
        if (this.selectedView == "acvStep") {
            echartsData.sort((a: any, b: any) => {
                return this.order.indexOf(a.data) - this.order.indexOf(b.data);
            });
        } else {
            // Sort by alphabetical order
            echartsData.sort((a: any, b: any) => a.data.localeCompare(b.data));
        }
        this.echartsRepo.setMainChart(echartsData);
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
