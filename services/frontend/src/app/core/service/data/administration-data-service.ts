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
import {
    Subscriber,
    OrganizationUpsertRest,
} from "../../interfaces/administration.interfaces";

const endpoint = environment.apiEndpoints.subscribers;
const endpointForOrg = environment.apiEndpoints.organizations;
const endpointForUser = environment.apiEndpoints.users;

@Injectable({
    providedIn: "root",
})
export class AdministrationDataService {
    constructor(private http: HttpClient) { }

    getOrganizations(): Observable<Subscriber> {
        return this.http.get<Subscriber>(`${endpoint}`);
    }

    getUsers(): Observable<Subscriber> {
        return this.http.get<Subscriber>(`${endpointForOrg}`);
    }

    deleteOrganization(
        organizationId: number,
        body: OrganizationUpsertRest,
    ): Observable<OrganizationUpsertRest> {
        return this.http.put<OrganizationUpsertRest>(
            `${endpointForOrg}?organizationId=${organizationId}`,
            body,
        );
    }

    getUserDetails(organizationId: number): Observable<any> {
        return this.http.get<any>(`${endpointForOrg}/users?organizationId=${organizationId}`);
    }

    getSearchDetails(searchName: string, subscriberId: number, organizationId: number): Observable<any> {
        return this.http.get<any>(`${endpoint}/${endpointForUser}?searchedName=${searchName}&subscriberId=${subscriberId}&organizationId=${organizationId}`);
    }

    postOrganization(body: any): Observable<any> {
        return this.http.post<any>(`${endpointForOrg}/users`, body);
    }
}
