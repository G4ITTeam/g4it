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
import { DigitalServiceServerConfig } from "../../interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "../data/digital-services-data.service";
import { DigitalServiceBusinessService } from "./digital-services.service";
declare var require: any

describe("DigitalServiceBusinessService", () => {
    let httpMock: HttpTestingController;
    let digitalServiceService: DigitalServiceBusinessService;
    let digitalServiceDataService: DigitalServicesDataService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [DigitalServiceBusinessService, DigitalServicesDataService],
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

    it("should transform terminal footprint data correctly", () => {
        const terminalFootprint = require("mock-server/data/digital-service-data/digital_service_terminals_footprint.json");

        const transformedData =
            digitalServiceService.transformTerminalData(terminalFootprint);

        expect(transformedData.length).toBe(5);
        expect(transformedData[2].impactCountry[0].totalNbUsers).toBe(50);
        expect(Math.round(transformedData[2].impactCountry[0].totalSipValue)).toBe(0);
        expect(Math.round(transformedData[2].impactCountry[0].avgUsageTime)).toBe(120);
        expect(transformedData[4].impactType[0].totalNbUsers).toBe(450);
        expect(Math.round(transformedData[4].impactType[0].totalSipValue)).toBe(65);
        expect(Math.round(transformedData[4].impactType[0].avgUsageTime)).toBe(1293);
        expect(transformedData[1].impactType[0].impact[0].ACVStep).toBe("FABRICATION");
        expect(transformedData[1].impactType[0].impact[1].ACVStep).toBe("DISTRIBUTION");
        expect(transformedData[1].impactType[0].impact[2].ACVStep).toBe("UTILISATION");
        expect(transformedData[1].impactType[0].impact[3].ACVStep).toBe("FIN_DE_VIE");
    });
});
