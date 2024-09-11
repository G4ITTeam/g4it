/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";

import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import {
    ApplicationFootprint,
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowImpact,
} from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";
import { FootprintService } from "./footprint.service";

describe("FootprintService", () => {
    let service: FootprintService;
    let httpMock: HttpTestingController;
    let inventoryDate = 4;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [FootprintService, TranslatePipe, TranslateService],
        });
        service = TestBed.inject(FootprintService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    it("initDatacenters should retrieve datacenters", () => {
        const datacenter: Datacenter[] = [
            {
                dataCenterName: "DC_Villeperdue_01",
                physicalEquipmentCount: 1,
                country: null,
                entity: "ACME SERVICES",
                equipment: "Monitor",
                status: "Retired",
                pue: 1.0,
            },
            {
                dataCenterName: "Datacenter 5",
                physicalEquipmentCount: 3,
                country: "Germany",
                entity: "ACME FRANCE",
                equipment: "Printer",
                status: "On order",
                pue: 1.1,
            },
            {
                dataCenterName: "Datacenter 9",
                physicalEquipmentCount: 11,
                country: "France",
                entity: Constants.EMPTY,
                equipment: "Smartphone",
                status: "Missing",
                pue: 1.2,
            },
        ];

        service.initDatacenters(inventoryDate).subscribe((res) => {
            expect(res).toEqual(datacenter);
        });

        const req = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/datacenters`,
        );
        expect(req.request.method).toEqual("GET");
        req.flush(datacenter);

        httpMock.verify();
    });

    it("initPhysicalEquipments should retrieve avg Age and low impact of physical equipments", () => {
        const physicalEquipmentAvgAge: PhysicalEquipmentAvgAge[] = [
            {
                organisation: "SSG",
                inventoryDate: "04-2023",
                country: "France",
                type: "Monitor",
                nomEntite: null,
                statut: "Retired",
                poids: 50,
                ageMoyen: 1.5,
            },
            {
                organisation: "SSG",
                inventoryDate: "04-2023",
                country: "Spain",
                type: "Smartphone",
                nomEntite: "ACME FRANCE",
                statut: "Retired",
                poids: 70,
                ageMoyen: 1.8,
            },
            {
                organisation: "SSG",
                inventoryDate: "04-2023",
                country: "Germany",
                type: "Monitor",
                nomEntite: "ACME SERVICES",
                statut: "On order",
                poids: 200,
                ageMoyen: 1.3,
            },
        ];
        const physicalEquipmentLowImpact: PhysicalEquipmentLowImpact[] = [
            {
                inventoryDate: "04-2023",
                paysUtilisation: "France",
                type: "Monitor",
                nomEntite: null,
                statut: "Retired",
                quantite: 50,
                lowImpact: true,
            },
            {
                inventoryDate: "04-2023",
                paysUtilisation: "Spain",
                type: "Smartphone",
                nomEntite: "ACME FRANCE",
                statut: "Retired",
                quantite: 70,
                lowImpact: false,
            },
            {
                inventoryDate: "04-2023",
                paysUtilisation: "Germany",
                type: "Monitor",
                nomEntite: "ACME SERVICES",
                statut: "On order",
                quantite: 30,
                lowImpact: true,
            },
        ];

        service.initPhysicalEquipments(inventoryDate).subscribe((res) => {
            expect(res).toEqual([physicalEquipmentAvgAge, physicalEquipmentLowImpact]);
        });

        const req2 = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/physicalEquipmentsAvgAge`,
        );
        expect(req2.request.method).toEqual("GET");
        req2.flush(physicalEquipmentAvgAge);

        const req1 = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/physicalEquipmentsLowImpact`,
        );
        expect(req1.request.method).toEqual("GET");
        req1.flush(physicalEquipmentLowImpact);

        httpMock.verify();
    });

    it("should send an export request", () => {
        service.sendExportRequest(inventoryDate).subscribe();

        const req = httpMock.expectOne("inventories/" + inventoryDate + "/export");
        expect(req.request.method).toEqual("POST");
        req.flush(inventoryDate);

        httpMock.verify();
    });

    it("deleteIndicators() should work", () => {
        service.deleteIndicators(inventoryDate).subscribe();

        const req = httpMock.expectOne(`inventories/${inventoryDate}/indicators`);

        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });

    it("should set unspecified if applicationName  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: Constants.UNSPECIFIED,
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should set unspecified if environment  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: Constants.UNSPECIFIED,
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should set unspecified if equipmentType  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: Constants.UNSPECIFIED,
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should set unspecified if lifeCycle  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: Constants.UNSPECIFIED,
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should set unspecified if domain  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: Constants.UNSPECIFIED,
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should set unspecified if subdomain  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: Constants.UNSPECIFIED,
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should set unspecified if vmName  = '' ", () => {
        const footprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        const expectedfootprint: ApplicationFootprint[] = [
            {
                criteria: "particulate-matter",
                criteriaTitle: "Particulate Matter",
                unit: "Disease Incidence",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 1.16,
                        sip: 3.35,
                    },
                ],
            },
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiations",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App9",
                        domain: "Domain A",
                        subDomain: "subDomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Using",
                        impact: 0.04,
                        sip: 0.06,
                    },
                ],
            },
        ];

        let res = service.setUnspecifiedData(footprint);

        expect(res).toEqual(expectedfootprint);
    });

    it("should calculate an empty footprint depending on incoming footprint, filters and selectedView", () => {
        expect(service.calculate({}, {}, "view", [])).toEqual([]);
    });

    it("should calculate a footprint depending on incoming footprint, filters and selectedView", () => {
        const footprint: Criterias = {
            "climate-change": {
                label: "unknown",
                unit: "kg CO²",
                impacts: [
                    {
                        country: "France",
                        impact: 2,
                        sip: 1,
                        acvStep: "FABRICATION",
                        entity: "",
                        equipment: "",
                        status: "",
                    },
                    {
                        country: "Germany",
                        impact: 8,
                        sip: 4,
                        acvStep: "DISTRIBUTION",
                        entity: "",
                        equipment: "",
                        status: "",
                    },
                ],
            },
        };

        const expected = [
            {
                data: "France",
                impacts: [
                    {
                        criteria: "climate-change",
                        sumSip: 1,
                        sumImpact: 2,
                    },
                ],
                total: {
                    sip: 1,
                    impact: 2,
                },
            },
            {
                data: "Germany",
                impacts: [
                    {
                        criteria: "climate-change",
                        sumSip: 4,
                        sumImpact: 8,
                    },
                ],
                total: {
                    sip: 4,
                    impact: 8,
                },
            },
        ];

        // no filter
        expect(
            service.calculate(footprint, { country: [Constants.ALL] }, "country", [
                "country",
            ]),
        ).toEqual(expected);

        // filter on France should return the first element in an array
        expect(
            service.calculate(footprint, { country: ["France"] }, "country", ["country"]),
        ).toEqual([expected[0]]);
    });

    it("should calculate the total of empty footprint depending on unit", () => {
        expect(service.calculateTotal([], Constants.PEOPLEEQ)).toEqual(0);
    });

    it("should calculate the total of footprint depending on unit peopleeq", () => {
        expect(
            service.calculateTotal(
                [
                    {
                        data: "",
                        impacts: [],
                        total: {
                            impact: 1,
                            sip: 2,
                        },
                    },
                ],
                Constants.PEOPLEEQ,
            ),
        ).toEqual(2);
    });

    it("should calculate the total of footprint depending on unit impact", () => {
        expect(
            service.calculateTotal(
                [
                    {
                        data: "",
                        impacts: [],
                        total: {
                            impact: 1,
                            sip: 2,
                        },
                    },
                ],
                "impact",
            ),
        ).toEqual(1);
    });
});
