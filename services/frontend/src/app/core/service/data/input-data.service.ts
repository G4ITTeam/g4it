/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";
import { Constants } from "src/constants";
import { InVirtualEquipmentRest } from "../../interfaces/input.interface";

const endpoint = Constants.ENDPOINTS.digitalServices;

@Injectable({
    providedIn: "root",
})
export class InputDataService {
    private HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });
    constructor(private http: HttpClient) {}

    getVirtualEquipments(
        digitalServiceUid: string,
    ): Observable<InVirtualEquipmentRest[]> {
        return this.http.get<InVirtualEquipmentRest[]>(
            `${endpoint}/${digitalServiceUid}/inputs/virtual-equipments`,
        );
    }

    update(virtualEquipment: InVirtualEquipmentRest): Observable<InVirtualEquipmentRest> {
        return this.http.put<InVirtualEquipmentRest>(
            `${endpoint}/${virtualEquipment.digitalServiceUid}/inputs/virtual-equipments/${virtualEquipment.id}`,
            virtualEquipment,
            {
                headers: this.HEADERS,
            },
        );
    }

    create(virtualEquipment: InVirtualEquipmentRest): Observable<InVirtualEquipmentRest> {
        return this.http.post<InVirtualEquipmentRest>(
            `${endpoint}/${virtualEquipment.digitalServiceUid}/inputs/virtual-equipments`,
            virtualEquipment,
            { headers: this.HEADERS },
        );
    }

    delete(virtualEquipment: InVirtualEquipmentRest): Observable<InVirtualEquipmentRest> {
        return this.http.delete<InVirtualEquipmentRest>(
            `${endpoint}/${virtualEquipment.digitalServiceUid}/inputs/virtual-equipments/${virtualEquipment.id}`,
        );
    }
}
