/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClient, HttpParams } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Inventory, CreateInventory } from "src/app/core/interfaces/inventory.interfaces";
import { environment } from "src/environments/environment";

const endpoint = environment.apiEndpoints.inventories;

@Injectable({
    providedIn: "root",
})
export class InventoryDataService {
    constructor(private http: HttpClient) {}

    createInventory(creationObj: CreateInventory): Observable<Inventory> {
        return this.http.post<Inventory>(`${endpoint}`, creationObj);
    }
 
    getInventories(inventoryId?: number): Observable<Inventory[]> {
        if (inventoryId === undefined) {
            return this.http.get<Inventory[]>(`${endpoint}`);
        }

        let params = new HttpParams().append("inventoryId", inventoryId);

        return this.http.get<Inventory[]>(`${endpoint}`, {
            params,
        });
    }

    deleteInventory(id: number): Observable<Inventory[]> {
        return this.http.delete<any>(`${endpoint}/${id}`);
    }
}
