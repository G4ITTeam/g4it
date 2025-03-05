/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, SimpleChanges, computed, inject } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
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
    private translate = inject(TranslateService);

    overlayVisible: boolean = false;

    @Input() allFilters: Filter<string> = {};
    tabs = Constants.EQUIPMENT_FILTERS;
    all = Constants.ALL;
    empty = Constants.EMPTY;

    selectedFilterNames = computed(() => {
        const selectedFilters = this.footprintStore.filters();
        return Object.keys(selectedFilters)
            .filter((tab) => this.filterActive(selectedFilters[tab]))
            .map((tab) =>
                this.translate.instant(`inventories-footprint.filter-tabs.${tab}`),
            )
            .join(", ");
    });

    ngOnChanges(changes: SimpleChanges) {
        if (changes["allFilters"]) {
            this.footprintStore.setFilters(this.allFilters);
        }
    }

    filterActive(filter: any) {
        return (
            filter.length === 0 ||
            (typeof filter[0] === "object" && filter[0]["checked"] === false) ||
            (typeof filter[0] === "string" && !filter.includes("All"))
        );
    }

    onFilterSelected(selectedValues: string[], tab: string, selection: string) {
        const f = this.footprintStore.filters();
        f[tab] = this.filterService.getUpdateSelectedValues(
            selectedValues,
            this.allFilters[tab],
            selection,
        );
        this.footprintStore.setFilters(f);
    }
}
