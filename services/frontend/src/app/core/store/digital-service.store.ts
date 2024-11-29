import { computed, Injectable, signal } from "@angular/core";
import { MapString } from "../interfaces/generic.interfaces";

interface DigitalServiceState {
    enableCalcul: boolean;
    countryMap: MapString;
}

const initialState: DigitalServiceState = {
    enableCalcul: true,
    countryMap: {} as MapString,
};

@Injectable({
    providedIn: "root",
})
export class DigitalServiceStoreService {
    private readonly _store = signal(initialState);

    readonly enableCalcul = computed(() => this._store().enableCalcul);
    setEnableCalcul(enableCalcul: boolean) {
        this._store.update((s) => ({ ...s, enableCalcul }));
    }

    readonly countryMap = computed(() => this._store().countryMap);
    setCountryMap(countryMap: MapString) {
        this._store.update((s) => ({ ...s, countryMap }));
    }
}
