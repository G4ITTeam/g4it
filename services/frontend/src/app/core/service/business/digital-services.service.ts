/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Injectable } from "@angular/core";
import { ReplaySubject, lastValueFrom } from "rxjs";
import {
    DigitalService,
    DigitalServiceServerConfig,
    DigitalServiceTerminalsImpact,
} from "../../interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "./../data/digital-services-data.service";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";

@Injectable({
    providedIn: "root",
})
export class DigitalServiceBusinessService {
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
        digitalService: DigitalService
    ) {
        this.serverSubject.next(server);

        // Find the index of the server with the matching uid
        let existingServerIndex = digitalService.servers?.findIndex(
            (t) => t.uid === server.uid
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

    transformTerminalData(terminalFootprint: any): DigitalServiceTerminalsImpact[] {
        const transformedData: any[] = [];
        terminalFootprint.forEach((item: any) => {
            const order = LifeCycleUtils.getLifeCycleList();

            const criteria = item.criteria.split(" ").slice(0, 2).join(" ");
            const impactCountry: any[] = [];
            const impactType: any[] = [];

            item.impacts.forEach((impact: any) => {
                // Find existing impactCountry or create a new one
                const existingCountry = impactCountry.find(
                    (country) => country.name === impact.country
                );

                if (existingCountry) {
                    existingCountry.totalSipValue += impact.sipValue;
                    existingCountry.totalNbUsers += impact.numberUsers;
                    existingCountry.avgUsageTime +=
                        impact.numberUsers * impact.yearlyUsageTimePerUser;

                    // Find existing ACVStep or create a new one
                    const existingACVStep = existingCountry.impact.find(
                        (step: any) => step.ACVStep === impact.acvStep
                    );

                    if (existingACVStep) {
                        existingACVStep.sipValue += impact.sipValue;
                    } else {
                        existingCountry.impact.push({
                            ACVStep: impact.acvStep,
                            sipValue: impact.sipValue,
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
                        impact: [
                            {
                                ACVStep: impact.acvStep,
                                sipValue: impact.sipValue,
                            },
                        ],
                    };
                    impactCountry.push(newCountry);
                }

                // Find existing impactType or create a new one
                const existingType = impactType.find(
                    (type) => type.name === impact.description
                );

                if (existingType) {
                    existingType.totalSipValue += impact.sipValue;
                    existingType.totalNbUsers += impact.numberUsers;
                    existingType.avgUsageTime +=
                        impact.numberUsers * impact.yearlyUsageTimePerUser;

                    // Find existing ACVStep or create a new one
                    const existingACVStep = existingType.impact.find(
                        (step: any) => step.ACVStep === impact.acvStep
                    );

                    if (existingACVStep) {
                        existingACVStep.sipValue += impact.sipValue;
                    } else {
                        existingType.impact.push({
                            ACVStep: impact.acvStep,
                            sipValue: impact.sipValue,
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
                        impact: [
                            {
                                ACVStep: impact.acvStep,
                                sipValue: impact.sipValue,
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
}
