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
import { Version } from "../../interfaces/version.interfaces";

const endpoint = environment.apiEndpoints.version;

@Injectable({
    providedIn: "root",
})
export class VersionDataService {
    constructor(private http: HttpClient) {}

    getVersion(): Observable<Version> {
        return this.http.get<Version>(`${endpoint}`);
    }
}
