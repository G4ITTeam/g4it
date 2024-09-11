import { Injectable, computed, signal } from "@angular/core";
import { Constants } from "src/constants";
import { Filter } from "../interfaces/filter.interface";

interface ApplicationState {
    filters: Filter;
    unit: string;
    criteria: string;
}

const initialState: ApplicationState = {
    filters: {},
    unit: Constants.PEOPLEEQ,
    criteria: Constants.MUTLI_CRITERIA,
};

@Injectable({
    providedIn: "root",
})
export class FootprintStoreService {
    private readonly _store = signal(initialState);

    readonly filters = computed(() => this._store().filters);
    setFilters(filters: Filter) {
        this._store.update((s) => ({ ...s, filters: { ...filters } }));
    }

    readonly unit = computed(() => this._store().unit);
    setUnit(unit: string) {
        this._store.update((s) => ({ ...s, unit }));
    }

    readonly criteria = computed(() => this._store().criteria);
    setCriteria(criteria: string) {
        this._store.update((s) => ({ ...s, criteria }));
    }
}
