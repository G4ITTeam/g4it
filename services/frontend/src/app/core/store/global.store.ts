import { computed, Injectable, signal } from "@angular/core";

interface GlobalState {
    loading: boolean;
}

const initialState: GlobalState = {
    loading: false,
};

@Injectable({
    providedIn: "root",
})
export class GlobalStoreService {
    private readonly _store = signal(initialState);

    readonly loading = computed(() => this._store().loading);
    setLoading(loading: boolean) {
        if (loading) {
            this._store.update((s) => ({ ...s, loading }));
        } else {
            setTimeout(() => {
                this._store.update((s) => ({ ...s, loading }));
            }, 300);
        }
    }
}
