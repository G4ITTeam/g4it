/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component } from "@angular/core";
import { Subject, combineLatestWith, takeUntil } from "rxjs";
import { FilterRepository } from "src/app/core/store/filter.repository";
import {
    ApplicationFootprint,
    FootprintRepository,
} from "src/app/core/store/footprint.repository";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";

@Component({
    selector: "app-application-stats",
    templateUrl: "./application-stats.component.html",
})
export class ApplicationStatsComponent {
    selectedInventoryDate: string = "";
    footprint: ApplicationFootprint[] = [];
    noData: boolean = false;
    selectedEnvironnement: string[] = [];
    selectedLifecycle: string[] = [];
    selectedEquipments: string[] = [];
    selectedDomain: string[] = [];
    selectedSubDomain: string[] = [];
    domainFilter: string[] = [];
    appCount: number = 0;

    ngUnsubscribe = new Subject<void>();

    constructor(
        private appComponent: InventoriesApplicationFootprintComponent,
        private filterRepo: FilterRepository,
        private footprintRepo: FootprintRepository
    ) {}

    async ngOnInit(): Promise<void> {
        this.footprintRepo.applicationFootprint$
            .pipe(
                combineLatestWith(this.filterRepo.selectedApplicationFilters$),
                takeUntil(this.ngUnsubscribe)
            )
            .subscribe(([applications, selectedFilters]) => {
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
                this.footprint = this.appComponent.formatLifecycleImpact(applications);
                this.computeApplicationStats(
                    this.footprint,
                    this.selectedEnvironnement,
                    this.selectedEquipments,
                    this.selectedLifecycle,
                    this.selectedDomain,
                    this.selectedSubDomain
                );
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

    computeApplicationStats(
        applications: ApplicationFootprint[],
        selectedEnvironnement: string[],
        selectedEquipments: string[],
        selectedLifecycle: string[],
        selectedDomain: string[],
        selectedSubDomain: string[]
    ) {
        applications = applications || [];
        let applicationCount = 0;
        let appNameList: string[] = [];
        applications.forEach((application) => {
            application.impacts.forEach((impact) => {
                let {
                    environment,
                    equipmentType,
                    lifeCycle,
                    domain,
                    subDomain,
                    applicationName,
                } = impact;
                environment = environment || "Empty";
                equipmentType = equipmentType || "Empty";
                lifeCycle = lifeCycle || "Empty";
                domain = domain || "Empty";
                subDomain = subDomain || "Empty";
                if (
                    selectedEnvironnement.includes(environment) &&
                    selectedEquipments.includes(equipmentType) &&
                    selectedLifecycle.includes(lifeCycle) &&
                    selectedDomain.includes(domain) &&
                    selectedSubDomain.includes(subDomain) &&
                    !appNameList.includes(applicationName)
                ) {
                    appNameList.push(applicationName);
                    applicationCount += 1;
                }
            });
        });

        this.appCount = applicationCount;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
