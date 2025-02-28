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
import { InPhysicalEquipmentRest } from "../../../interfaces/input.interface";

const endpoint = Constants.ENDPOINTS.digitalServices;

@Injectable({
    providedIn: "root",
})
export class InPhysicalEquipmentsService {
    private HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });

    private API = "physical-equipments";

    constructor(private http: HttpClient) {}

    get(digitalServiceUid: string): Observable<InPhysicalEquipmentRest[]> {
        return this.http.get<InPhysicalEquipmentRest[]>(
            `${endpoint}/${digitalServiceUid}/inputs/${this.API}`,
        );
    }

    update(equipment: InPhysicalEquipmentRest): Observable<InPhysicalEquipmentRest> {
        return this.http.put<InPhysicalEquipmentRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}/${equipment.id}`,
            equipment,
            {
                headers: this.HEADERS,
            },
        );
    }

    create(equipment: InPhysicalEquipmentRest): Observable<InPhysicalEquipmentRest> {
        return this.http.post<InPhysicalEquipmentRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}`,
            equipment,
            { headers: this.HEADERS },
        );
    }

    delete(equipment: InPhysicalEquipmentRest): Observable<InPhysicalEquipmentRest> {
        return this.http.delete<InPhysicalEquipmentRest>(
            `${endpoint}/${equipment.digitalServiceUid}/inputs/${this.API}/${equipment.id}`,
        );
    }
}
