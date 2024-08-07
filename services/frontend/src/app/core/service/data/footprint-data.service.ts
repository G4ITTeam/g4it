/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, forkJoin } from "rxjs";
import { Constants } from "src/constants";
import { Filter, FilterApplicationReceived } from "../../store/filter.repository";
import {
    ApplicationCriteriaFootprint,
    ApplicationFootprint,
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
} from "../../store/footprint.repository";

const endpoint = Constants.ENDPOINTS.inventories;

@Injectable({
    providedIn: "root",
})
export class FootprintDataService {
    constructor(private http: HttpClient) {}

    getFootprint(inventoryId: number): Observable<Criterias> {
        return this.http.get<Criterias>(
            `${endpoint}/${inventoryId}/indicators/equipments`,
        );
    }

    getApplicationFootprint(inventoryId: number): Observable<ApplicationFootprint[]> {
        return this.http.get<ApplicationFootprint[]>(
            `${endpoint}/${inventoryId}/indicators/applications`,
        );
    }

    getApplicationCriteriaFootprint(
        inventoryId: number,
        app: string,
        criteria: string,
    ): Observable<ApplicationCriteriaFootprint[]> {
        return this.http.get<ApplicationCriteriaFootprint[]>(
            `${endpoint}/${inventoryId}/indicators/applications/${app}/${criteria}`,
        );
    }

    getFilters(inventoryId: number): Observable<Filter> {
        return this.http.get<Filter>(
            `${endpoint}/${inventoryId}/indicators/equipments/filters`,
        );
    }

    getApplicationFilters(
        inventoryId: number,
        domain = "",
        subDomain = "",
        applicationName = "",
    ): Observable<FilterApplicationReceived> {
        const obj: any = {};
        if (domain) obj.domain = domain;
        if (subDomain) obj.subDomain = subDomain;
        if (applicationName) obj.applicationName = applicationName;

        return this.http.get<FilterApplicationReceived>(
            `${endpoint}/${inventoryId}/indicators/applications/filters`,
            { params: new HttpParams({ fromObject: obj }) },
        );
    }

    getDatacenters(inventoryId: number) {
        return this.http.get<Datacenter[]>(
            `${endpoint}/${inventoryId}/indicators/datacenters`,
        );
    }

    getPhysicalEquipments(inventoryId: number) {
        const averageAge$ = this.http.get<PhysicalEquipmentAvgAge[]>(
            `${endpoint}/${inventoryId}/indicators/physicalEquipmentsAvgAge`,
        );
        const lowImpact$ = this.http.get<PhysicalEquipmentLowImpact[]>(
            `${endpoint}/${inventoryId}/indicators/physicalEquipmentsLowImpact`,
        );
        return forkJoin([averageAge$, lowImpact$]);
    }

    sendExportRequest(inventoryId: number): Observable<number> {
        return this.http.post<number>(`${endpoint}/${inventoryId}/export`, {});
    }

    deleteIndicators(inventoryId: number) {
        return this.http.delete<any>(`${endpoint}/${inventoryId}/indicators`);
    }

    downloadExportResultsFile(inventoryId: number): Observable<any> {
        return this.http.get(`${endpoint}/${inventoryId}/indicators/export/download`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }
}
