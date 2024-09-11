/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG } from "ngx-echarts";
import { TooltipModule } from "primeng/tooltip";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { FilterRepository } from "src/app/core/store/filter.repository";
import { InventoryRepository } from "src/app/core/store/inventory.repository";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationStatsComponent } from "./application-stats.component";

describe("ApplicationStatsComponent", () => {
    let component: ApplicationStatsComponent;
    let fixture: ComponentFixture<ApplicationStatsComponent>;
    let inventoryRepo: InventoryRepository;
    let dataServiceFootprint: FootprintDataService;
    let filterRepo: FilterRepository;
    let inventoryDate = "05-2023";

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [ApplicationStatsComponent],
            imports: [HttpClientTestingModule, TranslateModule.forRoot(), TooltipModule],
            providers: [
                TranslatePipe,
                TranslateService,
                InventoryRepository,
                FootprintDataService,
                InventoriesApplicationFootprintComponent,
                FilterRepository,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: {
                                get: () => inventoryDate,
                            },
                        },
                    },
                },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(ApplicationStatsComponent);
        component = fixture.componentInstance;
        inventoryRepo = TestBed.inject(InventoryRepository);
        dataServiceFootprint = TestBed.inject(FootprintDataService);
        filterRepo = TestBed.inject(FilterRepository);
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should return the right domain and subdomain filter", () => {
        //GIVEN
        component.domainFilter = [
            Constants.ALL,
            "Domain A,subdomain 1a,subdomain 2a",
            "Domain B,subdomain 1b,subdomain 2b",
            "Domain C,subdomain 1c,subdomain 2c",
            "Domain D,subdomain 1d,subdomain 2d",
        ];

        //WHEN
        component.initDomainFilter();

        //THEN
        expect(component.selectedDomain).toEqual([
            Constants.ALL,
            "Domain A",
            "Domain B",
            "Domain C",
            "Domain D",
        ]);
        expect(component.selectedSubDomain).toEqual([
            "subdomain 1a",
            "subdomain 2a",
            "subdomain 1b",
            "subdomain 2b",
            "subdomain 1c",
            "subdomain 2c",
            "subdomain 1d",
            "subdomain 2d",
        ]);
    });

    it("should return the right number of application when selected in filters", () => {
        //GIVEN
        component.selectedEnvironnement = [Constants.ALL, "Production", "Preproduction"];
        component.selectedEquipments = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycle = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomain = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomain = ["subdomain 1", "subdomain 2", "subdomain 3"];
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 2",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                    {
                        applicationName: "App Bb",
                        domain: "Domain B",
                        subDomain: "subdomain 3",
                        environment: "Preproduction",
                        equipmentType: "Smartphone",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                ],
            },
        ];

        //WHEN
        component.computeApplicationStats(
            component.footprint,
            component.selectedEnvironnement,
            component.selectedEquipments,
            component.selectedLifecycle,
            component.selectedDomain,
            component.selectedSubDomain,
        );

        //THEN
        expect(component.appCount).toEqual(3);
    });

    it("should return 0 application if no environnement filter selected", () => {
        //GIVEN
        component.selectedEnvironnement = [];
        component.selectedEquipments = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycle = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomain = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomain = ["subdomain 1", "subdomain 2", "subdomain 3"];
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 2",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                    {
                        applicationName: "App Bb",
                        domain: "Domain B",
                        subDomain: "subdomain 3",
                        environment: "Preproduction",
                        equipmentType: "Smartphone",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                ],
            },
        ];

        //WHEN
        component.computeApplicationStats(
            component.footprint,
            component.selectedEnvironnement,
            component.selectedEquipments,
            component.selectedLifecycle,
            component.selectedDomain,
            component.selectedSubDomain,
        );

        //THEN
        expect(component.appCount).toEqual(0);
    });

    it("should return 0 application if no equipment filter selected", () => {
        //GIVEN
        component.selectedEnvironnement = [Constants.ALL, "Production", "Preproduction"];
        component.selectedEquipments = [];
        component.selectedLifecycle = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomain = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomain = ["subdomain 1", "subdomain 2", "subdomain 3"];
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 2",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                    {
                        applicationName: "App Bb",
                        domain: "Domain B",
                        subDomain: "subdomain 3",
                        environment: "Preproduction",
                        equipmentType: "Smartphone",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                ],
            },
        ];

        //WHEN
        component.computeApplicationStats(
            component.footprint,
            component.selectedEnvironnement,
            component.selectedEquipments,
            component.selectedLifecycle,
            component.selectedDomain,
            component.selectedSubDomain,
        );

        //THEN
        expect(component.appCount).toEqual(0);
    });

    it("should return 0 application if no lifecycle filter selected", () => {
        //GIVEN
        component.selectedEnvironnement = [Constants.ALL, "Production", "Preproduction"];
        component.selectedEquipments = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycle = [];
        component.selectedDomain = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomain = ["subdomain 1", "subdomain 2", "subdomain 3"];
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 2",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                    {
                        applicationName: "App Bb",
                        domain: "Domain B",
                        subDomain: "subdomain 3",
                        environment: "Preproduction",
                        equipmentType: "Smartphone",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                ],
            },
        ];

        //WHEN
        component.computeApplicationStats(
            component.footprint,
            component.selectedEnvironnement,
            component.selectedEquipments,
            component.selectedLifecycle,
            component.selectedDomain,
            component.selectedSubDomain,
        );

        //THEN
        expect(component.appCount).toEqual(0);
    });

    it("should return 0 application if no domain filter selected", () => {
        //GIVEN
        component.selectedEnvironnement = [Constants.ALL, "Production", "Preproduction"];
        component.selectedEquipments = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycle = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomain = [];
        component.selectedSubDomain = ["subdomain 1", "subdomain 2", "subdomain 3"];
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 2",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                    {
                        applicationName: "App Bb",
                        domain: "Domain B",
                        subDomain: "subdomain 3",
                        environment: "Preproduction",
                        equipmentType: "Smartphone",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                ],
            },
        ];

        //WHEN
        component.computeApplicationStats(
            component.footprint,
            component.selectedEnvironnement,
            component.selectedEquipments,
            component.selectedLifecycle,
            component.selectedDomain,
            component.selectedSubDomain,
        );

        //THEN
        expect(component.appCount).toEqual(0);
    });

    it("should return 0 application if no subdomain filter selected", () => {
        //GIVEN
        component.selectedEnvironnement = [Constants.ALL, "Production", "Preproduction"];
        component.selectedEquipments = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycle = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomain = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomain = [];
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 2",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                    {
                        applicationName: "App Bb",
                        domain: "Domain B",
                        subDomain: "subdomain 3",
                        environment: "Preproduction",
                        equipmentType: "Smartphone",
                        lifeCycle: "Using",
                        impact: 1.9,
                        sip: 1.4,
                    },
                ],
            },
        ];

        //WHEN
        component.computeApplicationStats(
            component.footprint,
            component.selectedEnvironnement,
            component.selectedEquipments,
            component.selectedLifecycle,
            component.selectedDomain,
            component.selectedSubDomain,
        );

        //THEN
        expect(component.appCount).toEqual(0);
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
