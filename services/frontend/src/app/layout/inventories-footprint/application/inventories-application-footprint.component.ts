/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component } from "@angular/core";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MenuItem } from "primeng/api";
import { Subject, takeUntil } from "rxjs";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import {
    FilterRepository,
} from "src/app/core/store/filter.repository";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
} from "src/app/core/store/footprint.repository";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import * as CriteriaUtils from "src/app/core/utils/criteria";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-application-footprint",
    templateUrl: "./inventories-application-footprint.component.html",
})
export class InventoriesApplicationFootprintComponent {
    ngUnsubscribe = new Subject<void>();
    selectedCriteria: string = "";
    criteres: MenuItem[] = [
        {
            label: this.translate.instant("criteria.multi-criteria.title"),
            routerLink: "multi-criteria",
        },
        ...CriteriaUtils.getCriteriaShortList().map(criteria => {
            return {
                label: this.translate.instant(`criteria.${criteria}.title`),
                routerLink: criteria,
            }
        })
    ];

    constructor(
        private activatedRoute: ActivatedRoute,
        public filterRepo: FilterRepository,
        private router: Router,
        public footprintService: FootprintService,
        private spinner: NgxSpinnerService,
        private translate: TranslateService,
        public inventoryRepo: InventoryRepository,
        public echartsRepo: EchartsRepository
    ) {}

    async ngOnInit(): Promise<void> {
        this.spinner.show();
        this.echartsRepo.setIsDataInitialized(false);
        this.echartsRepo.isDataInitialized$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((chartInitialized: boolean) => {
                if (chartInitialized) {
                    this.spinner.hide();
                }
            });
        // Set active inventory based on route
        const selectedInventoryId =
            this.activatedRoute.snapshot.paramMap.get("inventoryId") ?
                parseInt(this.activatedRoute.snapshot.paramMap.get("inventoryId")!) : 0;
        //Set footprint with associated filters and retrieve datacenter and physical equipement datas
        this.inventoryRepo.updateSelectedInventory(selectedInventoryId);

        this.footprintService.retrieveFootprint(
            selectedInventoryId,
            this.getCriteriaFromUrl(),
            "application"
        );
        this.filterRepo.selectedCriteria$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((criteria: string) => {
                if (criteria) {
                    this.selectedCriteria = criteria;
                }
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

    formatLifecycles(lifeCycles: string[]): string[] {
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        return lifeCycles.map((lifeCycle) => {
            lifeCycle = lifeCycle.replace("acvStep.", "");
            if (
                lifeCycle !== "All" &&
                lifeCycle !== Constants.UNSPECIFIED &&
                lifecyclesList.includes(lifeCycle)
            ) {
                return this.translate.instant("acvStep." + lifecycleMap.get(lifeCycle));
            } else {
                return lifeCycle;
            }
        });
    }

    formatLifecycleImpact(footprint: ApplicationFootprint[]): ApplicationFootprint[] {
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        footprint.forEach((element) => {
            element.impacts.forEach((impact) => {
                if (
                    impact.lifeCycle !== "All" &&
                    impact.lifeCycle !== Constants.UNSPECIFIED &&
                    lifecyclesList.includes(impact.lifeCycle)
                ) {
                    impact.lifeCycle = this.translate.instant(
                        "acvStep." + lifecycleMap.get(impact.lifeCycle)
                    );
                }
            });
        });
        return footprint;
    }

    formatLifecycleCriteriaImpact(
        footprint: ApplicationCriteriaFootprint[]
    ): ApplicationCriteriaFootprint[] {
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        footprint.forEach((element) => {
            element.impacts.forEach((impact) => {
                if (
                    impact.lifeCycle !== "All" &&
                    impact.lifeCycle !== Constants.UNSPECIFIED &&
                    lifecyclesList.includes(impact.lifeCycle)
                ) {
                    impact.lifeCycle = this.translate.instant(
                        "acvStep." + lifecycleMap.get(impact.lifeCycle)
                    );
                }
            });
        });
        return footprint;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
