/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { createStore, select, setProp, withProps } from "@ngneat/elf";
import { Constants } from "src/constants";

export interface Filter {
    countries: string[];
    entities: string[];
    equipments: string[];
    status: string[];
}

interface FilterProps {
    selectedCriteria: string;
    selectedUnite: string;
    selectedView: string;
    selectedFilters: Filter;
    allFilters: Filter;
}

const filterStore = createStore(
    { name: "Filter" },
    withProps<FilterProps>({
        selectedCriteria: "",
        selectedUnite: Constants.PEOPLEEQ,
        selectedView: Constants.ACV_STEP,
        selectedFilters: {
            countries: [],
            entities: [],
            status: [],
            equipments: [],
        },
        allFilters: {
            countries: [],
            entities: [],
            status: [],
            equipments: [],
        },
    }),
);

export interface FilterApplicationReceived {
    environments: string[];
    types: string[];
    lifeCycles: string[];
    domains: FilterApplicationDomain[];
}

export interface FilterApplicationDomain {
    name: string;
    subDomains: string[];
}

interface FilterPropsApp {
    selectedCriteria: string;
    selectedFilters: FilterApplication;
    allFilters: FilterApplication;
}

export interface FilterApplication {
    environments: string[];
    types: string[];
    lifeCycles: string[];
    domains: string[];
    subdomains?: string[];
}

const filterStoreApp = createStore(
    { name: "FilterApplication" },
    withProps<FilterPropsApp>({
        selectedCriteria: "",
        selectedFilters: {
            environments: [],
            types: [],
            lifeCycles: [],
            domains: [],
        },
        allFilters: {
            environments: [],
            types: [],
            lifeCycles: [],
            domains: [],
        },
    }),
);

@Injectable({ providedIn: "root" })
export class FilterRepository {
    selectedCriteria$ = filterStore.pipe(select((state) => state.selectedCriteria));

    selectedView$ = filterStore.pipe(select((state) => state.selectedView));

    selectedUnite$ = filterStore.pipe(select((state) => state.selectedUnite));

    selectedFilters$ = filterStore.pipe(select((state) => state.selectedFilters));

    allFilters$ = filterStore.pipe(select((state) => state.allFilters));

    selectedApplicationFilters$ = filterStoreApp.pipe(
        select((state) => state.selectedFilters),
    );

    allApplicationFilters$ = filterStoreApp.pipe(select((state) => state.allFilters));

    updateSelectedCriteria(filter: FilterProps["selectedCriteria"]) {
        filterStore.update(setProp("selectedCriteria", filter));
    }

    updateSelectedView(view: FilterProps["selectedView"]) {
        filterStore.update(setProp("selectedView", view));
    }

    updateSelectedUnite(unit: FilterProps["selectedUnite"]) {
        filterStore.update(setProp("selectedUnite", unit));
    }

    updateSelectedFilters(filters: Filter) {
        filterStore.update(setProp("selectedFilters", filters));
    }

    setAllFilters(filters: Filter) {
        filterStore.update(setProp("allFilters", filters));
    }

    updateSelectedCriteriaApp(filter: FilterPropsApp["selectedCriteria"]) {
        filterStoreApp.update(setProp("selectedCriteria", filter));
    }

    updateSelectedFiltersApp(filters: FilterApplication) {
        filterStoreApp.update(setProp("selectedFilters", filters));
    }

    setAllFiltersApp(filters: FilterApplication) {
        filterStoreApp.update(setProp("allFilters", filters));
    }
}
