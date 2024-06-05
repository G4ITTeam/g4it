/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */

import { Injectable } from "@angular/core";
import { AdministrationDataService } from "../data/administration-data-service";
import { Observable } from "rxjs";
import { OrganizationUpsertRest, Subscriber } from "../../interfaces/administration.interfaces";

@Injectable({
    providedIn: "root",
})
export class AdministrationService {

    iSModuleValues: { [key: string]: any } = {
        1: "ROLE_INVENTORY_READ",
        2: "ROLE_INVENTORY_WRITE",
    }

    dSModuleValues: { [key: string]: any } = {
        1: "ROLE_DIGITAL_SERVICE_READ",
        2: "ROLE_DIGITAL_SERVICE_WRITE",
    }

    constructor(private administrationDataService: AdministrationDataService) { }

    getOrganizations(): Observable<Subscriber> {
        return this.administrationDataService.getOrganizations();
    }

    getUsers(): Observable<Subscriber> {
        return this.administrationDataService.getUsers();
    }

    deleteOrganization(organizationId: number, body: OrganizationUpsertRest): Observable<any> {
        return this.administrationDataService.deleteOrganization(organizationId, body);
    }

    getUserDetails(organizationId: number): Observable<any> {
        return this.administrationDataService.getUserDetails(organizationId);
    }

    getSearchDetails(searchName: string, subscriberId: number, organizationId: number): Observable<any> {
        return this.administrationDataService.getSearchDetails(searchName, subscriberId, organizationId);
    }

    postOrganization(body: any): Observable<any> {
        return this.administrationDataService.postOrganization(body);
    }

    updateISModuleValue(id: number): void {
        return this.iSModuleValues[id];
    }

    updateDSModuleValue(id: number): void {
        return this.dSModuleValues[id];
    }
}
