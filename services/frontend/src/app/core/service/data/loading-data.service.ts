/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { LoadingBody, LoadingFile } from "../../interfaces/file-system.interfaces";

const endpoint = environment.apiEndpoints.inventories;

@Injectable({
    providedIn: "root",
})
export class LoadingDataService {
    data: LoadingBody | undefined;

    constructor(private http: HttpClient, private translate: TranslateService) {}

    launchLoading(fileList: LoadingFile[], inventoryId: number): Observable<number> {
        const headers = new HttpHeaders({
            "content-type": "application/json",
            "Accept-Language": this.translate.currentLang,
        });
        const body = JSON.stringify(fileList);
        return this.http.post<number>(`${endpoint}/${inventoryId}/loading`, body, {
            headers: headers,
        });
    }
}
