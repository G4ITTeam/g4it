/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { Subject, takeUntil } from "rxjs";

import { Filter, FilterRepository } from "src/app/core/store/filter.repository";

@Component({
    selector: "dataviz-filter",
    templateUrl: "./dataviz-filter.component.html",
})
export class DatavizFilterComponent implements OnInit {
    overlayVisible: boolean = false;
    selectedCountries: string[] = [];
    selectedEntities: string[] = [];
    selectedEquipments: string[] = [];
    selectedStatus: string[] = [];

    filters: Filter = {
        countries: [],
        entities: [],
        equipments: [],
        status: [],
    };

    ngUnsubscribe = new Subject<void>();

    constructor(private filterRepo: FilterRepository) { }

    ngOnInit(): void {
        this.filterRepo.allFilters$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((filters) => {
                this.filters = filters;
                if (localStorage.getItem("selectedFiltersData")) {
                    let selectedValues = JSON.parse(localStorage.getItem("selectedFiltersData") || '{}');
                    this.selectedCountries = selectedValues.countries;
                    this.selectedEntities = selectedValues.entities;
                    this.selectedEquipments = selectedValues.equipments;
                    this.selectedStatus = selectedValues.status;
                } else {
                    this.selectedCountries = filters.countries;
                    this.selectedEntities = filters.entities;
                    this.selectedEquipments = filters.equipments;
                    this.selectedStatus = filters.status;
                }

            });
    }

    onFilterUpdate(data: string, value: string) {
        switch (data) {
            case "country":
                this.selectedCountries = this.updateSelectedValues(
                    this.selectedCountries,
                    this.filters.countries,
                    value
                );
                break;
            case "entity":
                this.selectedEntities = this.updateSelectedValues(
                    this.selectedEntities,
                    this.filters.entities,
                    value
                );
                break;
            case "equipment":
                this.selectedEquipments = this.updateSelectedValues(
                    this.selectedEquipments,
                    this.filters.equipments,
                    value
                );
                break;
            case "status":
                this.selectedStatus = this.updateSelectedValues(
                    this.selectedStatus,
                    this.filters.status,
                    value
                );
                break;
            default:
                return;
        }
        this.filterRepo.updateSelectedFilters({
            countries: this.selectedCountries,
            entities: this.selectedEntities,
            equipments: this.selectedEquipments,
            status: this.selectedStatus,
        });
        let selectedFilterValues = ({
            countries: this.selectedCountries,
            entities: this.selectedEntities,
            equipments: this.selectedEquipments,
            status: this.selectedStatus,
        });
        localStorage.setItem("selectedFiltersData", JSON.stringify(selectedFilterValues))
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

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
