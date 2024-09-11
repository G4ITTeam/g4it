/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, inject } from "@angular/core";
import { ActivatedRoute, NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { Subject, takeUntil } from "rxjs";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { EchartsRepository } from "src/app/core/store/echarts.repository";
import { FilterRepository } from "src/app/core/store/filter.repository";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
} from "src/app/core/store/footprint.repository";
import { GlobalStoreService } from "src/app/core/store/global.store";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";

@Component({
    selector: "app-inventories-application-footprint",
    templateUrl: "./inventories-application-footprint.component.html",
})
export class InventoriesApplicationFootprintComponent {
    private global = inject(GlobalStoreService);

    ngUnsubscribe = new Subject<void>();
    selectedCriteria: string = "";
    criteres: MenuItem[] = [
        {
            label: this.translate.instant("criteria.multi-criteria.title"),
            routerLink: Constants.MUTLI_CRITERIA,
        },
        ...Constants.CRITERIAS.map((criteria) => {
            return {
                label: this.translate.instant(`criteria.${criteria}.title`),
                routerLink: criteria,
            };
        }),
    ];
    inventoryId!: number;

    constructor(
        private activatedRoute: ActivatedRoute,
        public filterRepo: FilterRepository,
        private router: Router,
        public footprintService: FootprintService,
        private translate: TranslateService,
        public inventoryRepo: InventoryRepository,
        public echartsRepo: EchartsRepository,
    ) {}

    async ngOnInit(): Promise<void> {
        this.global.setLoading(true);
        this.echartsRepo.setIsDataInitialized(false);
        this.echartsRepo.isDataInitialized$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((chartInitialized: boolean) => {
                if (chartInitialized) {
                    this.global.setLoading(false);
                }
            });
        // Set active inventory based on route
        this.inventoryId = +this.activatedRoute.snapshot.paramMap.get("inventoryId")!;

        //Set footprint with associated filters and retrieve datacenter and physical equipement datas
        this.inventoryRepo.updateSelectedInventory(this.inventoryId);

        this.footprintService.retrieveFootprint(
            this.inventoryId,
            this.getCriteriaFromUrl(),
            "application",
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
                lifeCycle !== Constants.ALL &&
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
                    impact.lifeCycle !== Constants.ALL &&
                    impact.lifeCycle !== Constants.UNSPECIFIED &&
                    lifecyclesList.includes(impact.lifeCycle)
                ) {
                    impact.lifeCycle = this.translate.instant(
                        "acvStep." + lifecycleMap.get(impact.lifeCycle),
                    );
                }
            });
        });
        return footprint;
    }

    formatLifecycleCriteriaImpact(
        footprint: ApplicationCriteriaFootprint[],
    ): ApplicationCriteriaFootprint[] {
        const lifecycleMap = LifeCycleUtils.getLifeCycleMap();
        const lifecyclesList = Array.from(lifecycleMap.keys());

        footprint.forEach((element) => {
            element.impacts.forEach((impact) => {
                if (
                    impact.lifeCycle !== Constants.ALL &&
                    impact.lifeCycle !== Constants.UNSPECIFIED &&
                    lifecyclesList.includes(impact.lifeCycle)
                ) {
                    impact.lifeCycle = this.translate.instant(
                        "acvStep." + lifecycleMap.get(impact.lifeCycle),
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
