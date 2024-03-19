/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, Input, SimpleChanges } from "@angular/core";
import { Subject, takeUntil } from "rxjs";
import {
    ApplicationCriteriaFootprint,
    ApplicationCriteriaImpact,
    ApplicationFootprint,
    ApplicationImpact,
    FootprintRepository,
} from "src/app/core/store/footprint.repository";

@Component({
    selector: "app-criteria-stats",
    templateUrl: "./criteria-stats.component.html",
})
export class CriteriaStatsComponent {
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
    appCount: number = 0;
    averageImpactSip: number = 0;
    averageImpactUnit: number = 0;

    ngUnsubscribe = new Subject<void>();

    constructor(private footprintRepo: FootprintRepository) {}

    ngOnInit() {
        this.footprintRepo.applicationCriteriaFootprint$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((criteriaFootprint) => {
                this.criteriaFootprint = criteriaFootprint;
                if (this.selectedGraph === "application") {
                    this.computeApplicationStatsAppGraph();
                } else {
                    this.computeApplicationStats();
                }
            });
    }

    ngOnChanges(changes: SimpleChanges): void {
        if (changes) {
            if (this.selectedGraph === "application") {
                this.computeApplicationStatsAppGraph();
            } else {
                this.computeApplicationStats();
            }
        }
    }

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

    computeApplicationStats() {
        let sipAvgImpact = 0;
        let unitAvgImpact = 0;
        let count = 0;
        let appNameList: Set<string> = new Set();
        this.appCount = 0;
        this.averageImpactSip = 0;
        this.averageImpactUnit = 0;
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
                                appNameList.add(impact.applicationName);
                                count++;
                                sipAvgImpact += impact.sip;
                                unitAvgImpact += impact.impact;
                                break;
                            case "domain":
                                if (this.selectedDomain.includes(impact.domain)) {
                                    appNameList.add(impact.applicationName);
                                    count++;
                                    sipAvgImpact += impact.sip;
                                    unitAvgImpact += impact.impact;
                                }
                                break;
                            case "subdomain":
                                if (
                                    this.selectedDomain.includes(impact.domain) &&
                                    this.selectedSubdomain.includes(impact.subDomain)
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
        this.appCount = appNameList.size;
        if (this.appCount !== 0) {
            this.averageImpactSip = sipAvgImpact / this.appCount;
            this.averageImpactUnit = unitAvgImpact / this.appCount;
        } else {
            this.averageImpactSip = 0;
            this.averageImpactUnit = 0;
        }
    }

    computeApplicationStatsAppGraph() {
        let sipAvgImpact = 0;
        let unitAvgImpact = 0;
        this.averageImpactSip = 0;
        this.averageImpactUnit = 0;
        this.criteriaFootprint.forEach((application) => {
            if (application.criteria === this.selectedCriteriaUri) {
                application.impacts.forEach((impact: ApplicationCriteriaImpact) => {
                    if (
                        this.selectedEnvironnementFilter.includes(impact.environment) &&
                        this.selectedEquipmentsFilter.includes(impact.equipmentType) &&
                        this.selectedLifecycleFilter.includes(impact.lifeCycle)
                    ) {
                        sipAvgImpact += impact.sip;
                        unitAvgImpact += impact.impact;
                    }
                });
            }
        });

        this.averageImpactSip = sipAvgImpact;
        this.averageImpactUnit = unitAvgImpact;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
