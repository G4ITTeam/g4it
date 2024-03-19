/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Subject, takeUntil } from "rxjs";

import {
    FilterApplication,
    FilterRepository,
} from "src/app/core/store/filter.repository";
import { Constants } from "src/constants";


@Component({
    selector: "dataviz-filter-application",
    templateUrl: "./dataviz-filter-application.component.html",
})
export class DatavizFilterApplicationComponent implements OnInit {
    overlayVisible: boolean = false;
    selectedEnvironnement: string[] = [];
    selectedEquipment: string[] = [];
    selectedlifecycle: string[] = [];
    selectedDomain: string[] = [];
    selectedSubdomain: string[] = [];

    filters: FilterApplication = {
        environments: [],
        types: [],
        lifeCycles: [],
        domains: [],
        subdomains: [],
    };

    domain2add: any = {
        label: "",
        children: [
            {
                label: "",
            },
        ],
    };
    domains: any[] = [];
    selectedValuesFilterDomain: any;
    selectedObjectDomain: any[] = [];
    selectedObjectSubDomain: any[] = [];

    ngUnsubscribe = new Subject<void>();

    constructor(
        private filterRepo: FilterRepository,
        private translate: TranslateService
    ) {}

    ngOnInit(): void {
        this.filterRepo.allApplicationFilters$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((filters) => {
                this.filters = filters;
                this.selectedEnvironnement = filters.environments;
                this.selectedEquipment = filters.types;
                this.selectedlifecycle = filters.lifeCycles;
                this.filters.domains = filters.domains;
                this.FormatLifecycleFilter();
                this.allDomainFiltersSelected();
            });
    }

    FormatLifecycleFilter() {
        this.selectedlifecycle.forEach((lifecycle, index) => {
            if (lifecycle !== "All" && lifecycle !== Constants.UNSPECIFIED) {
                this.selectedlifecycle[index] = this.translate.instant(
                    "acvStep." + lifecycle
                );
            }
        });
    }

    allDomainFiltersSelected() {
        this.domains = [];
        this.selectedObjectDomain = [];
        this.selectedObjectSubDomain = [];
        this.filters.domains.forEach((domain) => {
            this.domain2add = {
                label: "",
                children: [],
            };
            const splitDomain = domain.split(",");
            this.domain2add.label = splitDomain[0];
            splitDomain.shift();
            if (splitDomain !== undefined) {
                splitDomain.forEach((subDomain) => {
                    const sub2add = {
                        label: "",
                        domain: "",
                    };
                    sub2add.label = subDomain;
                    sub2add.domain = this.domain2add.label;
                    this.domain2add.children.push(sub2add);
                    this.selectedObjectSubDomain.push(sub2add);
                });
            }
            this.domains.push(this.domain2add);
            this.selectedObjectDomain.push(this.domain2add);
        });
        this.selectedValuesFilterDomain = this.selectedObjectDomain.concat(
            this.selectedObjectSubDomain
        );
        this.fillSelectedDomainAndSubDomain();
        this.saveFilters();
    }

    onFilterUpdate(data: string, value: string) {
        switch (data) {
            case "environnement":
                this.selectedEnvironnement = this.updateSelectedValues(
                    this.selectedEnvironnement,
                    this.filters.environments,
                    value
                );
                break;
            case "equipment":
                this.selectedEquipment = this.updateSelectedValues(
                    this.selectedEquipment,
                    this.filters.types,
                    value
                );
                break;
            case "lifecycle":
                this.selectedlifecycle = this.updateSelectedValues(
                    this.selectedlifecycle,
                    this.filters.lifeCycles,
                    value
                );
                break;
            default:
                return;
        }
        this.saveFilters();
    }

    updateSelectedValues(
        selectedValues: string[],
        allPossibleValues: string[],
        selection: string
    ): string[] {
        // The trick is : selectedValues is already updated
        // We only have to handle the "All" value manually...
        // Case 1: user toggles the "All" value
        if (selection === "All") {
            // case 1.1 : Select All Countries
            if (selectedValues.includes("All")) return [...allPossibleValues];

            // case 1.2 : Deselect All Countries
            return [];
        }
        // Case 2: user toggles a value other than "All"
        if (selectedValues.includes("All")) {
            // case 2.1 : All Countries were selected, and we deselect one.
            // we have to deselect "All" as well
            let result = [...selectedValues];
            result.splice(result.indexOf("All"), 1);
            return result;
        }
        if (selectedValues.length === allPossibleValues.length - 1) {
            // case 2.2 : All Countries but one were selected, and we select missing one.
            // we have to select "All" as well
            return [...allPossibleValues];
        }
        // in all other cases, we just have to let the selectedCountries as is
        return selectedValues;
    }

    toggle() {
        this.overlayVisible = !this.overlayVisible;
    }

    nodeSelect(event: any) {
        if (event.node.label === "All") {
            this.selectedValuesFilterDomain = [];
            this.allDomainFiltersSelected();
        } else {
            this.selectedDomain = [];
            this.selectedSubdomain = [];
            this.fillSelectedDomainAndSubDomain();
            this.addAllFilterValue();
            this.saveFilters();
        }
    }

    nodeUnselect(event: any) {
        if (event.node.label === "All") {
            this.selectedValuesFilterDomain = [];
        } else {
            this.selectedDomain = [];
            this.selectedSubdomain = [];
            if (this.selectedValuesFilterDomain[0].label === "All") {
                this.selectedValuesFilterDomain.shift();
            }
        }
        this.fillSelectedDomainAndSubDomain();
        this.saveFilters();
    }

    fillSelectedDomainAndSubDomain() {
        this.selectedSubdomain = [];
        this.selectedDomain = [];
        this.selectedValuesFilterDomain.forEach((value: any) => {
            if (
                value.children !== undefined &&
                !this.selectedDomain.includes(value.label)
            ) {
                this.selectedDomain.push(value.label);
            } else if (value.domain !== undefined) {
                this.selectedSubdomain.push(value.label);
                if (!this.selectedDomain.includes(value.domain)) {
                    this.selectedDomain.push(value.domain);
                }
            }
        });
    }

    addAllFilterValue() {
        if (
            this.selectedSubdomain.length === this.selectedObjectSubDomain.length &&
            this.selectedDomain.length === this.selectedObjectDomain.length - 1
        ) {
            this.selectedDomain.splice(0, 0, "All");
            this.selectedValuesFilterDomain.splice(0, 0, this.domains[0]);
        }
    }

    saveFilters() {
        this.filterRepo.updateSelectedFiltersApp({
            environments: this.selectedEnvironnement,
            types: this.selectedEquipment,
            lifeCycles: this.selectedlifecycle,
            domains: this.selectedDomain,
            subdomains: this.selectedSubdomain,
        });
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
