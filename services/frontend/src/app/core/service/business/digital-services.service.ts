/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable, inject } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { Observable, ReplaySubject, lastValueFrom, map } from "rxjs";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { Constants } from "src/constants";
import { removeBlankSpaces } from "../../custom-validators/unique-name.validator";
import {
    CloudImpact,
    CloudsImpact,
    DSCriteriaRest,
    DigitalService,
    DigitalServiceCloudImpact,
    DigitalServiceCloudResponse,
    DigitalServiceFootprint,
    DigitalServiceNetworksImpact,
    DigitalServiceServerConfig,
    DigitalServiceServersImpact,
    DigitalServiceTerminalResponse,
    DigitalServiceTerminalsImpact,
    ServerImpact,
    ServersType,
} from "../../interfaces/digital-service.interfaces";
import { MapString } from "../../interfaces/generic.interfaces";
import { DigitalServicesDataService } from "./../data/digital-services-data.service";

@Injectable({
    providedIn: "root",
})
export class DigitalServiceBusinessService {
    private translate = inject(TranslateService);
    private serverSubject = new ReplaySubject<DigitalServiceServerConfig>(1);
    serverFormSubject$ = this.serverSubject.asObservable();

    private panelSubject = new ReplaySubject<boolean>(1);
    panelSubject$ = this.panelSubject.asObservable();

    private dataInitializedSubject = new ReplaySubject<boolean>(1);
    dataInitializedSubject$ = this.dataInitializedSubject.asObservable();

    constructor(private digitalServiceData: DigitalServicesDataService) {}

    setDataInitialized(param: boolean) {
        this.dataInitializedSubject.next(param);
    }

    closePanel() {
        this.panelSubject.next(false);
    }

    openPanel() {
        this.panelSubject.next(true);
    }

    setServerForm(server: DigitalServiceServerConfig) {
        this.serverSubject.next(server);
    }

    async submitServerForm(
        server: DigitalServiceServerConfig,
        digitalService: DigitalService,
    ) {
        this.serverSubject.next(server);

        // Find the index of the server with the matching uid
        let existingServerIndex = digitalService.servers?.findIndex(
            (t) => t.uid === server.uid,
        );
        // If the server with the uid exists, update it; otherwise, add the new server
        if (
            existingServerIndex !== -1 &&
            existingServerIndex !== undefined &&
            digitalService.servers &&
            server.uid !== undefined
        ) {
            digitalService.servers[existingServerIndex] = server;
        } else {
            digitalService.servers.push(server);
        }

        await lastValueFrom(this.digitalServiceData.update(digitalService));
    }

    transformTerminalData(
        terminalFootprint: DigitalServiceTerminalResponse[],
    ): DigitalServiceTerminalsImpact[] {
        const transformedData: any[] = [];
        terminalFootprint.forEach((item) => {
            const order = LifeCycleUtils.getLifeCycleList();

            const criteria = item.criteria.split(" ").slice(0, 2).join(" ");
            const impactCountry: any[] = [];
            const impactType: any[] = [];

            item.impacts.forEach((impact: any) => {
                // Find existing impactCountry or create a new one
                const existingCountry = impactCountry.find(
                    (country) => country.name === impact.country,
                );

                if (existingCountry) {
                    existingCountry.totalSipValue += impact.sipValue;
                    existingCountry.totalNbUsers += impact.numberUsers;
                    existingCountry.avgUsageTime +=
                        impact.numberUsers * impact.yearlyUsageTimePerUser;
                    existingCountry.rawValue += impact.rawValue;
                    existingCountry.unit = impact.unit;

                    // Find existing ACVStep or create a new one
                    const existingACVStep = existingCountry.impact.find(
                        (step: any) => step.ACVStep === impact.acvStep,
                    );

                    if (existingACVStep) {
                        existingACVStep.sipValue += impact.sipValue;
                        existingACVStep.rawValue += impact.rawValue;
                        existingACVStep.unit = impact.unit;
                        existingACVStep.statusCount.ok +=
                            impact.status === Constants.DATA_QUALITY_STATUS.ok
                                ? impact.countValue
                                : 0;
                        existingACVStep.statusCount.error +=
                            impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                ? impact.countValue
                                : 0;
                        existingACVStep.statusCount.total += impact.countValue;
                    } else {
                        existingCountry.impact.push({
                            ACVStep: impact.acvStep,
                            sipValue: impact.sipValue,
                            rawValue: impact.rawValue,
                            unit: impact.unit,
                            status: impact.status,
                            statusCount: {
                                ok:
                                    impact.status === Constants.DATA_QUALITY_STATUS.ok
                                        ? impact.countValue
                                        : 0,
                                error:
                                    impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                        ? impact.countValue
                                        : 0,
                                total: impact.countValue,
                            },
                        });
                    }
                    existingCountry.impact.sort((a: any, b: any) => {
                        return order.indexOf(a.ACVStep) - order.indexOf(b.ACVStep);
                    });
                } else {
                    const newCountry = {
                        name: impact.country,
                        totalSipValue: impact.sipValue,
                        totalNbUsers: impact.numberUsers,
                        avgUsageTime: impact.numberUsers * impact.yearlyUsageTimePerUser,
                        rawValue: impact.rawValue,
                        unit: impact.unit,
                        impact: [
                            {
                                ACVStep: impact.acvStep,
                                sipValue: impact.sipValue,
                                rawValue: impact.rawValue,
                                unit: impact.unit,
                                status: impact.status,
                                statusCount: {
                                    ok:
                                        impact.status === Constants.DATA_QUALITY_STATUS.ok
                                            ? impact.countValue
                                            : 0,
                                    error:
                                        impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                            ? impact.countValue
                                            : 0,
                                    total: impact.countValue,
                                },
                            },
                        ],
                    };
                    impactCountry.push(newCountry);
                }

                // Find existing impactType or create a new one
                const existingType = impactType.find(
                    (type) => type.name === impact.description,
                );

                if (existingType) {
                    existingType.totalSipValue += impact.sipValue;
                    existingType.totalNbUsers += impact.numberUsers;
                    existingType.avgUsageTime +=
                        impact.numberUsers * impact.yearlyUsageTimePerUser;
                    existingType.rawValue += impact.rawValue;
                    existingType.unit = impact.unit;

                    // Find existing ACVStep or create a new one
                    const existingACVStep = existingType.impact.find(
                        (step: any) => step.ACVStep === impact.acvStep,
                    );

                    if (existingACVStep) {
                        existingACVStep.sipValue += impact.sipValue;
                        existingACVStep.rawValue += impact.rawValue;
                        existingACVStep.unit = impact.unit;
                        existingACVStep.statusCount.ok +=
                            impact.status === Constants.DATA_QUALITY_STATUS.ok
                                ? impact.countValue
                                : 0;
                        existingACVStep.statusCount.error +=
                            impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                ? impact.countValue
                                : 0;
                        existingACVStep.statusCount.total += impact.countValue;
                    } else {
                        existingType.impact.push({
                            ACVStep: impact.acvStep,
                            sipValue: impact.sipValue,
                            rawValue: impact.rawValue,
                            unit: impact.unit,
                            status: impact.status,
                            statusCount: {
                                ok:
                                    impact.status === Constants.DATA_QUALITY_STATUS.ok
                                        ? impact.countValue
                                        : 0,
                                error:
                                    impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                        ? impact.countValue
                                        : 0,
                                total: impact.countValue,
                            },
                        });
                    }

                    existingType.impact.sort((a: any, b: any) => {
                        return order.indexOf(a.ACVStep) - order.indexOf(b.ACVStep);
                    });
                } else {
                    const newType = {
                        name: impact.description,
                        totalSipValue: impact.sipValue,
                        totalNbUsers: impact.numberUsers,
                        avgUsageTime: impact.numberUsers * impact.yearlyUsageTimePerUser,
                        rawValue: impact.rawValue,
                        unit: impact.unit,
                        impact: [
                            {
                                ACVStep: impact.acvStep,
                                sipValue: impact.sipValue,
                                rawValue: impact.rawValue,
                                unit: impact.unit,
                                status: impact.status,
                                statusCount: {
                                    ok:
                                        impact.status === Constants.DATA_QUALITY_STATUS.ok
                                            ? impact.countValue
                                            : 0,
                                    error:
                                        impact.status !== Constants.DATA_QUALITY_STATUS.ok
                                            ? impact.countValue
                                            : 0,
                                    total: impact.countValue,
                                },
                            },
                        ],
                    };
                    impactType.push(newType);
                }
            });

            impactCountry.forEach((impact: any) => {
                impact.avgUsageTime = impact.avgUsageTime / impact.totalNbUsers;
                //Cancel the addition of users per acvStep (4 of them)
                impact.totalNbUsers = impact.totalNbUsers / 4;
            });
            impactType.forEach((impact: any) => {
                impact.avgUsageTime = impact.avgUsageTime / impact.totalNbUsers;
                //Cancel the addition of users per acvStep (4 of them)
                impact.totalNbUsers = impact.totalNbUsers / 4;
            });

            // Sort by name
            impactCountry.sort((a, b) => a.name.localeCompare(b.name));
            impactType.sort((a, b) => a.name.localeCompare(b.name));

            transformedData.push({
                criteria: criteria,
                impactCountry: impactCountry,
                impactType: impactType,
            });
        });
        return transformedData;
    }

    transformCloudData(
        cloudFootprint: DigitalServiceCloudResponse[],
        countryMap: MapString,
    ): DigitalServiceCloudImpact[] {
        const transformedData: DigitalServiceCloudImpact[] = [];
        const order = LifeCycleUtils.getLifeCycleList();

        cloudFootprint.forEach((item) => {
            const criteria = item.criteria.split(" ").slice(0, 2).join(" ");
            const impactLocation: CloudsImpact[] = [];
            const impactInstance: CloudsImpact[] = [];

            item.impacts
                .map((i) => ({ ...i, averageWorkLoad: i.averageWorkLoad * 100 }))
                .forEach((impact: CloudImpact) => {
                    this.processImpact(
                        impact,
                        impactLocation,
                        order,
                        "country",
                        countryMap,
                    );
                    this.processImpact(
                        impact,
                        impactInstance,
                        order,
                        "instance",
                        countryMap,
                    );
                });

            this.finalizeImpacts(impactLocation);
            this.finalizeImpacts(impactInstance);

            transformedData.push({
                criteria,
                impactLocation,
                impactInstance,
            });
        });

        return transformedData;
    }

    private processImpact(
        impact: CloudImpact,
        impactArray: CloudsImpact[],
        order: string[],
        type: "country" | "instance",
        countryMap: MapString,
    ): void {
        const name =
            type === "country"
                ? countryMap[impact.country]
                : `${impact.cloudProvider?.toUpperCase()}-${impact.instanceType}`;
        const existingImpact = impactArray.find((item) => item.name === name);

        if (existingImpact) {
            this.updateExistingImpact(existingImpact, impact, order);
        } else {
            impactArray.push(this.createNewImpact(name, impact));
        }
    }

    private updateExistingImpact(
        existingImpact: any,
        impact: any,
        order: string[],
    ): void {
        existingImpact.totalSipValue += impact.sipValue;
        existingImpact.rawValue += impact.rawValue;
        existingImpact.unit = impact.unit;
        existingImpact.totalQuantity += impact.quantity;
        existingImpact.totalAvgUsage += impact.averageUsage;
        existingImpact.totalAvgWorkLoad += impact.averageWorkLoad * impact.quantity;

        const existingACVStep = existingImpact.impact.find(
            (step: any) => step.acvStep === impact.acvStep,
        );

        if (existingACVStep) {
            existingACVStep.sipValue += impact.sipValue;
            existingACVStep.rawValue += impact.rawValue;
            existingACVStep.unit = impact.unit;
            existingACVStep.statusCount.ok +=
                impact.status === Constants.DATA_QUALITY_STATUS.ok
                    ? impact.countValue
                    : 0;
            existingACVStep.statusCount.error +=
                impact.status !== Constants.DATA_QUALITY_STATUS.ok
                    ? impact.countValue
                    : 0;
            existingACVStep.statusCount.total += impact.countValue;
        } else {
            existingImpact.impact.push(this.createACVStep(impact));
        }

        existingImpact.impact.sort(
            (a: any, b: any) => order.indexOf(a.acvStep) - order.indexOf(b.acvStep),
        );
    }

    private createNewImpact(name: string, impact: CloudImpact): CloudsImpact {
        return {
            name,
            totalSipValue: impact.sipValue,
            totalQuantity: impact.quantity,
            totalAvgUsage: impact.averageUsage,
            totalAvgWorkLoad: impact.averageWorkLoad * impact.quantity,
            rawValue: impact.rawValue,
            unit: impact.unit,
            impact: [this.createACVStep(impact)],
        };
    }

    private createACVStep(impact: CloudImpact) {
        return {
            acvStep: impact.acvStep,
            sipValue: impact.sipValue,
            rawValue: impact.rawValue,
            unit: impact.unit,
            status: impact.status,
            statusCount: {
                ok:
                    impact.status === Constants.DATA_QUALITY_STATUS.ok
                        ? impact.countValue
                        : 0,
                error:
                    impact.status !== Constants.DATA_QUALITY_STATUS.ok
                        ? impact.countValue
                        : 0,
                total: impact.countValue,
            },
        };
    }

    private finalizeImpacts(impacts: CloudsImpact[]) {
        impacts.forEach((impact) => {
            impact.totalAvgWorkLoad = impact.totalAvgWorkLoad / impact.totalQuantity;
            impact.totalAvgUsage = impact.totalAvgUsage / impact.totalQuantity;
        });

        impacts.sort((a, b) => a.name.localeCompare(b.name));
    }

    getFootprint(uid: DigitalService["uid"]): Observable<DigitalServiceFootprint[]> {
        return this.digitalServiceData
            .getFootprint(uid)
            .pipe(map((footprint) => this.transformFootprintCriteriaUnit(footprint)));
    }

    getTerminalsIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceTerminalResponse[]> {
        return this.digitalServiceData
            .getTerminalsIndicators(uid)
            .pipe(
                map((networkFootprint) =>
                    this.transformTerminalNetworkCriteriaUnit(networkFootprint),
                ),
            );
    }

    getNetworksIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceNetworksImpact[]> {
        return this.digitalServiceData
            .getNetworksIndicators(uid)
            .pipe(
                map((terminalFootprint) =>
                    this.transformTerminalNetworkCriteriaUnit(terminalFootprint),
                ),
            );
    }

    getServersIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceServersImpact[]> {
        return this.digitalServiceData
            .getServersIndicators(uid)
            .pipe(
                map((serverFootprint) =>
                    this.transformServerCriteriaUnit(serverFootprint),
                ),
            );
    }

    getCloudsIndicators(
        uid: DigitalService["uid"],
    ): Observable<DigitalServiceCloudResponse[]> {
        return this.digitalServiceData
            .getCloudsIndicators(uid)
            .pipe(
                map((cloudFootprint) =>
                    this.transformTerminalNetworkCriteriaUnit(cloudFootprint),
                ),
            );
    }

    transformFootprintCriteriaUnit(
        footprint: DigitalServiceFootprint[],
    ): DigitalServiceFootprint[] {
        return footprint.map((f) => ({
            ...f,
            impacts: f.impacts.map((fi) => ({
                ...fi,
                unit: this.translate.instant(`criteria.${fi.criteria}.unite`),
                unitValue: fi.unitValue ?? 0,
                sipValue: fi.sipValue ?? 0,
            })),
        }));
    }

    transformTerminalNetworkCriteriaUnit<
        T extends
            | DigitalServiceTerminalResponse
            | DigitalServiceNetworksImpact
            | DigitalServiceCloudResponse,
    >(footprint: T[]): T[] {
        return footprint.map((f) => ({
            ...f,
            impacts: f.impacts.map((fi) => ({
                ...fi,
                unit: this.translate.instant(`criteria.${f.criteria}.unite`),
                rawValue: fi.rawValue ?? 0,
                sipValue: fi.sipValue ?? 0,
            })),
        }));
    }

    transformServerCriteriaUnit(
        serverFootprint: DigitalServiceServersImpact[],
    ): DigitalServiceServersImpact[] {
        return serverFootprint.map((footprint) => ({
            ...footprint,
            impactsServer: footprint.impactsServer.map((impacts) => ({
                ...impacts,
                servers: this.transformServers(impacts, footprint),
            })),
        }));
    }

    transformServers(
        impacts: ServersType,
        footprint: DigitalServiceServersImpact,
    ): ServerImpact[] {
        return impacts.servers.map((server) => ({
            ...server,
            impactVmDisk: server.impactVmDisk.map((impact) => ({
                ...impact,
                unit: this.translate.instant(`criteria.${footprint.criteria}.unite`),
                rawValue: impact.rawValue ?? 0,
                sipValue: impact.sipValue ?? 0,
            })),
            impactStep: server.impactStep.map((impact) => ({
                ...impact,
                unit: this.translate.instant(`criteria.${footprint.criteria}.unite`),
                rawValue: impact.rawValue ?? 0,
                sipValue: impact.sipValue ?? 0,
            })),
        }));
    }

    updateDsCriteria(
        digitalServiceUid: string,
        DSCriteria: DSCriteriaRest,
    ): Observable<DSCriteriaRest> {
        return this.digitalServiceData.updateDsCriteria(digitalServiceUid, DSCriteria);
    }

    getNextAvailableName(existingNames: string[], baseName: string): string {
        const nameSet = new Set(existingNames?.map((n) => removeBlankSpaces(n)));
        let index = 1;
        let newName = `${removeBlankSpaces(baseName)}${String.fromCharCode(64 + index)}`; // Start with "ServerA"

        while (nameSet.has(newName)) {
            index++;
            newName = `${removeBlankSpaces(baseName)}${String.fromCharCode(64 + index)}`; // Increment to "ServerB", "ServerC", etc.
        }
        return `${baseName} ${String.fromCharCode(64 + index)}`;
    }
}
