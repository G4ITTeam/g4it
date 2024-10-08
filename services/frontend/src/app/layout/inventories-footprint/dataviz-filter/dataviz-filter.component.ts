/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, SimpleChanges, inject } from "@angular/core";
import { Filter } from "src/app/core/interfaces/filter.interface";
import { FilterService } from "src/app/core/service/business/filter.service";
import { FootprintStoreService } from "src/app/core/store/footprint.store";
import { Constants } from "src/constants";

@Component({
    selector: "dataviz-filter",
    templateUrl: "./dataviz-filter.component.html",
})
export class DatavizFilterComponent {
    protected footprintStore = inject(FootprintStoreService);
    private filterService = inject(FilterService);

    overlayVisible: boolean = false;

    @Input() allFilters: Filter<string> = {};
    tabs = Constants.EQUIPMENT_FILTERS;
    all = Constants.ALL;
    empty = Constants.EMPTY;

    ngOnChanges(changes: SimpleChanges) {
        if (changes["allFilters"]) {
            this.footprintStore.setFilters(this.allFilters);
        }
    }

    onFilterSelected(selectedValues: string[], tab: string, selection: string) {
        const f = this.footprintStore.filters();
        f[tab] = this.filterService.getUpdateSelectedValues(
            selectedValues,
            this.allFilters[tab] as string[],
            selection,
        );
        this.footprintStore.setFilters(f);
    }
}
