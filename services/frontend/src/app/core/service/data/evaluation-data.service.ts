/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { EvaluationBody } from "../../interfaces/inventory.interfaces";

const endpoint = environment.apiEndpoints.inventories;

@Injectable({
    providedIn: "root",
})
export class EvaluationDataService {
    data: EvaluationBody | undefined;

    constructor(private http: HttpClient) {}

    launchEstimation(inventoryId: number, organization: string): Observable<number> {
        const headers = { "content-type": "application/json" };
        this.data = {
            inventoryId: inventoryId,
            organization: organization,
        };
        const body = JSON.stringify(this.data);
        return this.http.post<number>(`${endpoint}/${inventoryId}/evaluation`, body, {
            headers: headers,
        });
    }
}
