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
import { Constants } from "src/constants";
import { LoadingBody } from "../../interfaces/file-system.interfaces";
import { TaskIdRest } from "../../interfaces/task.interfaces";

const endpoint = Constants.ENDPOINTS.inventories;

@Injectable({
    providedIn: "root",
})
export class LoadingDataService {
    data: LoadingBody | undefined;

    constructor(
        private http: HttpClient,
        private translate: TranslateService,
    ) {}

    launchLoadInputFiles(
        inventoryId: number,
        formData: FormData,
    ): Observable<TaskIdRest> {
        const headers = new HttpHeaders({
            "Accept-Language": this.translate.currentLang,
        });
        return this.http.post<TaskIdRest>(
            `${endpoint}/${inventoryId}/load-input-files`,
            formData,
            {
                headers,
            },
        );
    }
}
