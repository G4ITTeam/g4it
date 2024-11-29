/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { map, Observable, ReplaySubject, tap } from "rxjs";
import { Constants } from "src/constants";
import { environment } from "src/environments/environment";
import {
    DigitalService,
    DigitalServiceCloudResponse,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalResponse,
    DSCriteriaRest,
    Host,
    NetworkType,
    ServerDC,
    TerminalsType,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";

const endpoint = Constants.ENDPOINTS.digitalServices;

@Injectable({
    providedIn: "root",
})
export class DigitalServicesDataService {
    private HEADERS = new HttpHeaders({
        "content-type": "application/json",
    });
    constructor(private http: HttpClient) {}
    private digitalServiceSubject = new ReplaySubject<DigitalService>(1);
    digitalService$ = this.digitalServiceSubject.asObservable();

    list(): Observable<DigitalService[]> {
        return this.http.get<DigitalService[]>(`${endpoint}`);
    }

    create(): Observable<DigitalService> {
        return this.http.post<DigitalService>(
            `${endpoint}`,
            {},
            { headers: this.HEADERS },
        );
    }

    update(digitalService: DigitalService): Observable<DigitalService> {
        return this.http
            .put<DigitalService>(`${endpoint}/${digitalService.uid}`, digitalService, {
                headers: this.HEADERS,
            })
            .pipe(
                tap((res: DigitalService) => {
                    this.digitalServiceSubject.next(res);
                }),
            );
    }

    get(uid: DigitalService["uid"]): Observable<DigitalService> {
        return this.http
            .get<DigitalService>(`${endpoint}/${uid}`)
            .pipe(tap((res: DigitalService) => this.digitalServiceSubject.next(res)));
    }

    getFootprint(uid: DigitalService["uid"]): Observable<DigitalServiceFootprint[]> {
        return this.http.get<DigitalServiceFootprint[]>(`${endpoint}/${uid}/indicators`);
    }

    delete(uid: DigitalService["uid"]): Observable<string> {
        return this.http.delete<string>(`${endpoint}/${uid}`);
    }

    unlink(uid: DigitalService["uid"]): Observable<string> {
        return this.http.delete<string>(`${endpoint}/${uid}/share`);
    }

    getDeviceReferential(): Observable<TerminalsType[]> {
        return this.http.get<TerminalsType[]>(`${endpoint}/device-type`);
    }

    getCountryReferential(): Observable<string[]> {
        return this.http.get<string[]>(`${endpoint}/country`);
    }

    getNetworkReferential(): Observable<NetworkType[]> {
        return this.http.get<NetworkType[]>(`${endpoint}/network-type`);
    }

    getHostServerReferential(type: string): Observable<Host[]> {
        return this.http.get<Host[]>(`${endpoint}/server-host?type=${type}`);
    }

    getDatacenterServerReferential(uid: string): Observable<ServerDC[]> {
        return this.http.get<ServerDC[]>(`${endpoint}/${uid}/datacenters`);
    }
    getBoaviztapiCountryMap(): Observable<MapString> {
        return this.http.get<MapString>(`referential/boaviztapi/countries`);
    }
    getBoaviztapiCloudProviders(): Observable<string[]> {
        return this.http.get<string[]>(`referential/boaviztapi/cloud/providers`);
    }
    getBoaviztapiInstanceTypes(providerName: string): Observable<string[]> {
        return this.http.get<string[]>(
            `referential/boaviztapi/cloud/providers/instances?provider=${providerName}`,
        );
    }

    launchCalcul(uid: DigitalService["uid"]): Observable<string> {
        return this.http.post<string>(`${endpoint}/${uid}/evaluation`, {});
    }

    getTerminalsIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceTerminalResponse[]> {
        return this.http.get<DigitalServiceTerminalResponse[]>(
            `${endpoint}/${uid}/terminals/indicators`,
        );
    }

    getNetworksIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceNetworksImpact[]> {
        return this.http.get<DigitalServiceNetworksImpact[]>(
            `${endpoint}/${uid}/networks/indicators`,
        );
    }

    getServersIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceServersImpact[]> {
        return this.http.get<DigitalServiceServersImpact[]>(
            `${endpoint}/${uid}/servers/indicators`,
        );
    }

    getCloudsIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceCloudResponse[]> {
        return this.http.get<DigitalServiceCloudResponse[]>(
            `${endpoint}/${uid}/cloud/indicators`,
        );
    }

    copyUrl(uid: DigitalService["uid"]): Observable<string> {
        return this.http
            .post<string>(
                `${endpoint}/${uid}/share`,
                {},
                { responseType: "text" as "json" },
            )
            .pipe(map((response) => environment.frontEndUrl + response));
    }

    sharedDS(uid: string, generatedId: string): Observable<string> {
        return this.http.post<string>(`${endpoint}/${uid}/shared/${generatedId}`, {});
    }

    downloadFile(uid: DigitalService["uid"]): Observable<any> {
        return this.http.get(`${endpoint}/${uid}/export`, {
            responseType: "blob",
            headers: { Accept: "application/zip" },
        });
    }

    updateDsCriteria(
        digitalServiceUid: string,
        DSCriteria: DSCriteriaRest,
    ): Observable<DSCriteriaRest> {
        return this.http.put<DSCriteriaRest>(
            `${endpoint}/${digitalServiceUid}`,
            DSCriteria,
        );
    }
}
