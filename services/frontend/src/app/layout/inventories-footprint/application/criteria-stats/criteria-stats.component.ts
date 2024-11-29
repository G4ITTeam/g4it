/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, computed, inject, input, Signal } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Filter, TransformedDomain } from "src/app/core/interfaces/filter.interface";
import {
    ApplicationFootprint,
    ApplicationImpact,
} from "src/app/core/interfaces/footprint.interface";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";

interface CriteriaData {
    appCount: number;
    averageImpactSip: number;
    averageImpactUnit: number;
}

@Component({
    selector: "app-criteria-stats",
    templateUrl: "./criteria-stats.component.html",
})
export class CriteriaStatsComponent {
    protected footprintStore = inject(FootprintStoreService);
    private filterService = inject(FilterService);
    private translate = inject(TranslateService);

    selectedCriteria = computed(() => {
        return this.translate.instant(
            `criteria.${this.footprintStore.applicationCriteria()}`,
        );
    });

    footprint = input<ApplicationFootprint[]>([]);
    noData: boolean = false;
    isSip: boolean = true;

    criteriaSignal: Signal<CriteriaData> = computed(() => {
        return this.computeApplicationStats();
    });

    constructor(private appComponent: InventoriesApplicationFootprintComponent) {}

    updateSelectedUnite(typeOfUnit: string) {
        switch (typeOfUnit) {
            case "impact":
                this.isSip = false;
                break;
            case "peopleeq":
                this.isSip = true;
                break;
        }
    }

    computeApplicationStats(): CriteriaData {
        let sipAvgImpact = 0;
        let unitAvgImpact = 0;
        let count = 0;
        let appNameList: Set<string> = new Set();
        let appCount = 0;
        let averageImpactSip = 0;
        let averageImpactUnit = 0;
        this.footprint().forEach((application) => {
            if (application.criteria === this.footprintStore.applicationCriteria()) {
                application.impacts.forEach((impact: ApplicationImpact) => {
                    const domain = (
                        this.footprintStore.applicationSelectedFilters() as Filter<TransformedDomain>
                    )["domain"].find((d) => d?.label === impact.domain);

                    if (
                        this.filterService.getFilterincludes(
                            this.footprintStore.applicationSelectedFilters(),
                            impact,
                        )
                    ) {
                        switch (this.footprintStore.appGraphType()) {
                            case "global":
                                appNameList.add(impact.applicationName);
                                count++;
                                sipAvgImpact += impact.sip;
                                unitAvgImpact += impact.impact;
                                break;
                            case "domain":
                                if (domain?.checked) {
                                    appNameList.add(impact.applicationName);
                                    count++;
                                    sipAvgImpact += impact.sip;
                                    unitAvgImpact += impact.impact;
                                }
                                break;
                            case "subdomain":
                                if (
                                    domain?.children?.some(
                                        (child) =>
                                            child.label === impact.subDomain &&
                                            child.checked,
                                    )
                                ) {
                                    appNameList.add(impact.applicationName);
                                    count++;
                                    sipAvgImpact += impact.sip;
                                    unitAvgImpact += impact.impact;
                                }
                                break;
                            case "application":
                                if (
                                    this.footprintStore.appApplication() ===
                                    impact.applicationName
                                ) {
                                    appNameList.add(impact.applicationName);
                                    count++;
                                    sipAvgImpact += impact.sip;
                                    unitAvgImpact += impact.impact;
                                }
                                break;
                        }
                    }
                });
            }
        });

        appCount = appNameList.size;
        if (appCount !== 0) {
            averageImpactSip = sipAvgImpact / appCount;
            averageImpactUnit = unitAvgImpact / appCount;
        }

        return {
            appCount: appCount,
            averageImpactSip: averageImpactSip,
            averageImpactUnit: averageImpactUnit,
        };
    }
}
