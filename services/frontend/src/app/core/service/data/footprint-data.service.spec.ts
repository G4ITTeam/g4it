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

import {
    Criterias,
    Datacenter,
    PhysicalEquipmentAvgAge,
    PhysicalEquipmentLowCarbon,
} from "src/app/core/store/footprint.repository";
import { Filter } from "../../store/filter.repository";
import { FootprintDataService } from "./footprint-data.service";

describe("FootprintDataService", () => {
    let service: FootprintDataService;
    let httpMock: HttpTestingController;
    let inventoryDate = 4;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [FootprintDataService],
        });
        service = TestBed.inject(FootprintDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    it("getFootprint should retrieve Criterias", () => {
        const footprint: Criterias = {
            particule: {
                label: "particulate-matter",
                unit: "Disease incidence",
                impacts: [
                    {
                        acvStep: "DISTRIBUTION",
                        country: "China",
                        entity: "ACME SERVICES",
                        equipment: "Communication Device",
                        status: "In use",
                        impact: 2.1618197e-8,
                        sip: 3.3516585e-4,
                    },
                ],
            },
            radiation: {
                label: "Ionising radiation",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        acvStep: "DISTRIBUTION",
                        country: "China",
                        entity: "ACME SERVICES",
                        equipment: "Communication Device",
                        status: "In use",
                        impact: 0.004453057,
                        sip: 6.7598585e-8,
                    },
                ],
            },
            climate: {
                label: "Climate change",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        acvStep: "DISTRIBUTION",
                        country: "China",
                        entity: "ACME SERVICES",
                        equipment: "Communication Device",
                        status: "In use",
                        impact: 0.67612576,
                        sip: 7.9427403e-4,
                    },
                ],
            },
            acidification: {
                label: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        acvStep: "DISTRIBUTION",
                        country: "China",
                        entity: "ACME SERVICES",
                        equipment: "Communication Device",
                        status: "In use",
                        impact: 0.0036275906,
                        sip: 2.9020724e-5,
                    },
                ],
            },
            resource: {
                label: "Resources",
                unit: "mol H+ eq",
                impacts: [
                    {
                        acvStep: "DISTRIBUTION",
                        country: "China",
                        entity: "ACME SERVICES",
                        equipment: "Communication Device",
                        status: "In use",
                        impact: 0.0036275906,
                        sip: 2.9020724e-5,
                    },
                ],
            },
        };

        service.getFootprint(inventoryDate).subscribe((res) => {
            expect(res).toEqual(footprint);
        });

        const req = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/equipments`
        );
        expect(req.request.method).toEqual("GET");
        req.flush(footprint);

        httpMock.verify();
    });

    it("getDatacenters should retrieve datacenters", () => {
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
                entity: "Empty",
                equipment: "Smartphone",
                status: "Missing",
                pue: 1.2,
            },
        ];

        service.getDatacenters(inventoryDate).subscribe((res) => {
            expect(res).toEqual(datacenter);
        });

        const req = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/datacenters`
        );
        expect(req.request.method).toEqual("GET");
        req.flush(datacenter);

        httpMock.verify();
    });

    it("getPhysicalEquipments should retrieve avg Age and low carbon of physical equipments", () => {
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
        const physicalEquipmentLowCarbon: PhysicalEquipmentLowCarbon[] = [
            {
                organisation: "SSG",
                inventoryDate: "04-2023",
                paysUtilisation: "France",
                type: "Monitor",
                nomEntite: null,
                statut: "Retired",
                quantite: 50,
                lowCarbon: true,
            },
            {
                organisation: "SSG",
                inventoryDate: "04-2023",
                paysUtilisation: "Spain",
                type: "Smartphone",
                nomEntite: "ACME FRANCE",
                statut: "Retired",
                quantite: 70,
                lowCarbon: false,
            },
            {
                organisation: "SSG",
                inventoryDate: "04-2023",
                paysUtilisation: "Germany",
                type: "Monitor",
                nomEntite: "ACME SERVICES",
                statut: "On order",
                quantite: 30,
                lowCarbon: true,
            },
        ];

        service.getPhysicalEquipments(inventoryDate).subscribe((res) => {
            expect(res).toEqual([physicalEquipmentAvgAge, physicalEquipmentLowCarbon]);
        });

        const req2 = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/physicalEquipmentsAvgAge`
        );
        expect(req2.request.method).toEqual("GET");
        req2.flush(physicalEquipmentAvgAge);

        const req1 = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/physicalEquipmentsLowCarbon`
        );
        expect(req1.request.method).toEqual("GET");
        req1.flush(physicalEquipmentLowCarbon);

        httpMock.verify();
    });

    it("should send an export request", () => {
        service.sendExportRequest(inventoryDate).subscribe();

        const req = httpMock.expectOne(`inventories/${inventoryDate}/export`);
        expect(req.request.method).toEqual("POST");
        req.flush(inventoryDate);

        httpMock.verify();
    });

    it("getFilters should retrieve filters", () => {
        const filters: Filter = {
            countries: ["France", "China", "Spain", "Germany", "Russia"],
            entities: ["ACME FRANCE", "ACME SERVICES"],
            equipments: [
                "Mobility Device",
                "Monitor",
                "Network Gear",
                "Personal Computer",
                "Printer",
                "Smartphone",
                "Tracer",
                "Communication Device",
                "Consumable",
                "IP Router",
                "IP Switch",
                "Server",
            ],
            status: [
                "In use",
                "Retired",
                "Missing",
                "On order",
                "In maintenance",
                "In stock",
                "In transit",
                "Consumed",
            ],
        };

        service.getFilters(inventoryDate).subscribe((res) => {
            filters.countries.sort().splice(0, 0, "All");
            filters.entities.sort().splice(0, 0, "All");
            filters.equipments.sort().splice(0, 0, "All");
            filters.status.sort().splice(0, 0, "All");
            expect(res).toEqual(filters);
        });

        const req = httpMock.expectOne(
            `inventories/${inventoryDate}/indicators/equipments/filters`
        );
        expect(req.request.method).toEqual("GET");
        req.flush(filters);

        httpMock.verify();
    });

    it("deleteIndicators() should work", () => {
        service.deleteIndicators(inventoryDate).subscribe();

        const req = httpMock.expectOne(`inventories/${inventoryDate}/indicators`);

        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });
});
