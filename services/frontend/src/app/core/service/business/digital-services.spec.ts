/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { of } from "rxjs";
import {
    DigitalServiceFootprint,
    DigitalServiceServerConfig,
} from "../../interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "../data/digital-services-data.service";
import { DigitalServiceBusinessService } from "./digital-services.service";
declare var require: any;

describe("DigitalServiceBusinessService", () => {
    let httpMock: HttpTestingController;
    let digitalServiceService: DigitalServiceBusinessService;
    let digitalServiceDataService: DigitalServicesDataService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [
                DigitalServiceBusinessService,
                DigitalServicesDataService,
                TranslateService,
            ],
        });
        digitalServiceService = TestBed.inject(DigitalServiceBusinessService);
        digitalServiceDataService = TestBed.inject(DigitalServicesDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(digitalServiceService).toBeTruthy();
    });

    it("should emit server info to serverFormSubject$ Observable", () => {
        const serverData: DigitalServiceServerConfig = {
            uid: "lm0b2e0c-157c-4eb2-bb38-d81cer720e1c4",
            name: "Server A",
            mutualizationType: "Dedicated",
            type: "Storage",
            quantity: 3,
            host: {
                code: 2,
                value: "Server Storage M",
                characteristic: [],
            },
            datacenter: {
                uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                name: "Default DC",
                location: "France",
                pue: 1,
            },
            totalVCpu: 100,
            lifespan: 10.5,
            annualElectricConsumption: 1000,
            annualOperatingTime: 8760,
            vm: [],
        };

        digitalServiceService.serverFormSubject$.subscribe((server) => {
            expect(server).toBe(serverData);
        });

        digitalServiceService.setServerForm(serverData);
    });

    it("should close panel", () => {
        digitalServiceService.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        digitalServiceService.closePanel();
    });

    it("should open panel", () => {
        digitalServiceService.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(true);
        });

        digitalServiceService.openPanel();
    });

    it("should transform criteria unit", () => {
        const mockFootprint: DigitalServiceFootprint[] = [
            {
                tier: "c1",
                impacts: [
                    { criteria: "climate-change", unitValue: 10, sipValue: 1, unit: "" },
                    { criteria: "resource-use", unitValue: 20, sipValue: 2, unit: "" },
                ],
            },
        ];

        const transformedFootprint =
            digitalServiceService.transformFootprintCriteriaUnit(mockFootprint);

        expect(transformedFootprint).toEqual([
            {
                tier: "c1",
                impacts: [
                    {
                        criteria: "climate-change",
                        unitValue: 10,
                        sipValue: 1,
                        unit: "criteria.climate-change.unite",
                    },
                    {
                        criteria: "resource-use",
                        unitValue: 20,
                        sipValue: 2,
                        unit: "criteria.resource-use.unite",
                    },
                ],
            },
        ]);
    });

    it("should call getFootprint method of DigitalServicesDataService and transform the result", () => {
        const uid = "123";
        const footprint = require("mock-server/data/digital-service-data/digital_service_indicators_footprint.json");
        spyOn(digitalServiceService, "getFootprint").and.returnValue(of(footprint));

        const result = digitalServiceService.getFootprint(uid);

        expect(digitalServiceService.getFootprint).toHaveBeenCalledWith(uid);
        result.subscribe((footprint) => {
            expect(footprint.length).toEqual(3);
        });
    });

    it("should call getTerminalsIndicators method of DigitalServicesDataService and transform the result", () => {
        const uid = "123";
        const terminalFootprint = require("mock-server/data/digital-service-data/digital_service_terminals_footprint.json");
        spyOn(digitalServiceService, "getTerminalsIndicators").and.returnValue(
            of(terminalFootprint),
        );

        const result = digitalServiceService.getTerminalsIndicators(uid);

        expect(digitalServiceService.getTerminalsIndicators).toHaveBeenCalledWith(uid);
        result.subscribe((terminalIndicator) => {
            expect(terminalIndicator.length).toEqual(16);
            expect(terminalIndicator[0].criteria).toEqual("particulate-matter");
            expect(terminalIndicator[0].impacts[0].unit).toEqual("Disease incidence");
        });
    });

    it("should call getNetworksIndicators method of DigitalServicesDataService and transform the result", () => {
        const uid = "123";
        const networkFootprint = require("mock-server/data/digital-service-data/digital_service_networks_footprint.json");
        spyOn(digitalServiceService, "getNetworksIndicators").and.returnValue(
            of(networkFootprint),
        );

        const result = digitalServiceService.getNetworksIndicators(uid);

        expect(digitalServiceService.getNetworksIndicators).toHaveBeenCalledWith(uid);
        result.subscribe((networkIndicator) => {
            expect(networkIndicator.length).toEqual(16);
            expect(networkIndicator[0].criteria).toEqual("particulate-matter");
            expect(networkIndicator[0].impacts[0].unit).toEqual("Disease incidence");
        });
    });

    it("should call getServersIndicators method of DigitalServicesDataService and transform the result", () => {
        const uid = "123";
        const serverFootprint = require("mock-server/data/digital-service-data/digital_service_servers_footprint.json");
        spyOn(digitalServiceService, "getServersIndicators").and.returnValue(
            of(serverFootprint),
        );

        const result = digitalServiceService.getServersIndicators(uid);

        expect(digitalServiceService.getServersIndicators).toHaveBeenCalledWith(uid);
        result.subscribe((serverIndicator) => {
            expect(serverIndicator.length).toEqual(16);
            expect(serverIndicator[0].criteria).toEqual("climate-change");
            expect(
                serverIndicator[0].impactsServer[0].servers[0].impactVmDisk[0].unit,
            ).toEqual("mol H+ eq");
        });
    });
});
