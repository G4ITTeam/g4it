/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { SharedModule } from "primeng/api";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { FootprintService } from "src/app/core/service/business/footprint.service";
import { FootprintRepository } from "src/app/core/store/footprint.repository";
import { Constants } from "src/constants";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationCriteriaFootprintComponent } from "./application-criteria-footprint.component";

describe("ApplicationCriteriaFootprintComponent", () => {
    let component: ApplicationCriteriaFootprintComponent;
    let fixture: ComponentFixture<ApplicationCriteriaFootprintComponent>;
    let footprintRepo: FootprintRepository;
    let businessServiceFootprint: FootprintService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [ApplicationCriteriaFootprintComponent],
            imports: [
                HttpClientTestingModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
                RouterTestingModule,
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                InventoriesApplicationFootprintComponent,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
                IntegerPipe,
                DecimalsPipe,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(ApplicationCriteriaFootprintComponent);
        component = fixture.componentInstance;
        footprintRepo = TestBed.inject(FootprintRepository);
        businessServiceFootprint = TestBed.inject(FootprintService);
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should init domain filter", () => {
        component.selectedDomainFilter = ["Domain 1", "Domain 2"];
        component.selectedSubDomainFilter = ["subDomain 1", "subDomain 2"];
        component.domainFilter = [
            Constants.ALL,
            "Domain A,subdomain 1a,subdomain 2a",
            "Domain B,subdomain 1b,subdomain 2b",
        ];
        let expectedSelectedDomain = [Constants.ALL, "Domain A", "Domain B"];
        let expectedSelectedSubDomain = [
            "subdomain 1a",
            "subdomain 2a",
            "subdomain 1b",
            "subdomain 2b",
        ];

        component.initDomainFilter();

        expect(component.selectedDomainFilter).toEqual(expectedSelectedDomain);
        expect(component.selectedSubDomainFilter).toEqual(expectedSelectedSubDomain);
    });

    it("should get to the graph before from app to subdomain", () => {
        component.selectedGraph = "application";
        component.selectedApp = "app 1";
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
                criteriaTitle: "Resource Use",
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
        spyOn(component, "loadBarChartOption");
        spyOn(footprintRepo, "setSelectedGraph");
        spyOn(footprintRepo, "setSelectedApp");

        component.onArrowClick("application");

        expect(component.selectedGraph).toEqual("subdomain");
        expect(component.selectedApp).toEqual("");
        expect(footprintRepo.setSelectedApp).toHaveBeenCalledOnceWith("");
        expect(footprintRepo.setSelectedGraph).toHaveBeenCalledOnceWith("subdomain");
        expect(component.loadBarChartOption).toHaveBeenCalledTimes(1);
    });

    it("should get to the graph before from subdomain to domain", () => {
        component.selectedGraph = "subdomain";
        component.selectedSubdomain = "sub 1";
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
                criteriaTitle: "Resource Use",
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
        spyOn(component, "loadBarChartOption");
        spyOn(footprintRepo, "setSelectedGraph");
        spyOn(footprintRepo, "setSelectedSubdomain");

        component.onArrowClick("subdomain");

        expect(component.selectedGraph).toEqual("domain");
        expect(component.selectedSubdomain).toEqual("");
        expect(component.loadBarChartOption).toHaveBeenCalledTimes(1);
        expect(footprintRepo.setSelectedSubdomain).toHaveBeenCalledOnceWith("");
        expect(footprintRepo.setSelectedGraph).toHaveBeenCalledOnceWith("domain");
    });

    it("should get to the graph before from domain to global", () => {
        component.selectedGraph = "domain";
        component.selectedDomain = "domain 1";
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
                criteriaTitle: "Resource Use",
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
        spyOn(component, "loadBarChartOption");
        spyOn(footprintRepo, "setSelectedGraph");
        spyOn(footprintRepo, "setSelectedDomain");

        component.onArrowClick("domain");

        expect(component.selectedGraph).toEqual("global");
        expect(component.selectedDomain).toEqual("");
        expect(component.loadBarChartOption).toHaveBeenCalledTimes(1);
        expect(footprintRepo.setSelectedDomain).toHaveBeenCalledOnceWith("");
        expect(footprintRepo.setSelectedGraph).toHaveBeenCalledOnceWith("global");
    });

    it("should check if data available", () => {
        component.selectedEnvironnementFilter = [
            Constants.ALL,
            "Production",
            "Preproduction",
        ];
        component.selectedEquipmentsFilter = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomainFilter = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomainFilter = [];
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
                criteriaTitle: "Resource Use",
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

        let result = component.checkIfNoData();

        expect(result).toBeTrue();
    });

    it("should check that data is not available", () => {
        component.selectedEnvironnementFilter = [
            Constants.ALL,
            "Production",
            "Preproduction",
        ];
        component.selectedEquipmentsFilter = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomainFilter = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomainFilter = ["subdomain 1", "subdomain 2", "subdomain 3"];
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
                criteriaTitle: "Resource Use",
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

        let result = component.checkIfNoData();

        expect(result).toBeFalse();
    });

    it("should redirect to domain graph", () => {
        const event: any = {
            name: "domain 1",
        };
        component.selectedGraph = "global";
        component.selectedDomain = "";
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
                criteriaTitle: "Resource Use",
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
        spyOn(component, "loadBarChartOption");
        spyOn(footprintRepo, "setSelectedGraph");
        spyOn(footprintRepo, "setSelectedDomain");

        component.onChartClick(event);

        expect(component.selectedGraph).toEqual("domain");
        expect(component.loadBarChartOption).toHaveBeenCalledTimes(1);
        expect(footprintRepo.setSelectedDomain).toHaveBeenCalledOnceWith("domain 1");
        expect(footprintRepo.setSelectedGraph).toHaveBeenCalledOnceWith("domain");
    });

    it("should redirect to subdomain graph", () => {
        const event: any = {
            name: "subdomain 1",
        };
        component.selectedGraph = "domain";
        component.selectedSubdomain = "";
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
                criteriaTitle: "Resource Use",
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
        spyOn(component, "loadBarChartOption");
        spyOn(footprintRepo, "setSelectedGraph");
        spyOn(footprintRepo, "setSelectedSubdomain");

        component.onChartClick(event);

        expect(component.selectedGraph).toEqual("subdomain");
        expect(component.loadBarChartOption).toHaveBeenCalledTimes(1);
        expect(footprintRepo.setSelectedSubdomain).toHaveBeenCalledOnceWith(
            "subdomain 1",
        );
        expect(footprintRepo.setSelectedGraph).toHaveBeenCalledOnceWith("subdomain");
    });

    it("should redirect to application graph", () => {
        const event: any = {
            name: "app 1",
        };
        component.selectedGraph = "subdomain";
        component.selectedApp = "";
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
                criteriaTitle: "Resource Use",
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
        spyOn(component, "loadBarChartOption");
        spyOn(footprintRepo, "setSelectedGraph");
        spyOn(footprintRepo, "setSelectedApp");

        component.onChartClick(event);

        expect(component.selectedGraph).toEqual("application");
        expect(component.selectedApp).toEqual("app 1");
        expect(component.loadBarChartOption).toHaveBeenCalledTimes(0);
        expect(footprintRepo.setSelectedApp).toHaveBeenCalledOnceWith("app 1");
        expect(footprintRepo.setSelectedGraph).toHaveBeenCalledOnceWith("application");
    });

    it("should compute data case global and selected in filters", () => {
        const callParameters4ComputeImpactOrder = {
            applicationName: "App A",
            domain: "Domain A",
            subDomain: "subdomain 1",
            environment: "Production",
            equipmentType: "Laptop",
            lifeCycle: "Manufacturing",
            impact: 1.3,
            sip: 1.8,
        };
        component.footprint = [
            {
                criteria: "climate-change",
                criteriaTitle: "Climate Change",
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
                criteriaTitle: "Resource Use",
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
        component.selectedEnvironnementFilter = [
            Constants.ALL,
            "Production",
            "Preproduction",
        ];
        component.selectedGraph = "global";
        component.selectedCriteriaUri = "climate-change";
        component.selectedEquipmentsFilter = [Constants.ALL, "Laptop", "Smartphone"];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedDomainFilter = [Constants.ALL, "Domain A", "Domain B"];
        component.selectedSubDomainFilter = ["subdomain 1", "subdomain 2", "subdomain 3"];
        component.impactOrder = [
            {
                domain: "domain A",
                sipImpact: 1.8,
                unitImpact: 1.3,
                subdomain: "subdomain 1",
                app: "App A",
                equipment: "Laptop",
                environnement: "Production",
                subdomains: ["sudbomain 1"],
                apps: ["App A"],
                lifecycle: "USING",
            },
        ];
        const expectedResult = {
            xAxis: [1.8],
            yAxis: ["Domain A"],
            unitImpact: [1.3],
            subdomainCount: 1,
            appCount: 1,
            clusterList: ["cluster A"],
            equipmentList: ["Laptop"],
            environnementList: ["Production"],
        };
        spyOn(component, "computeImpactOrder");
        spyOn(component, "initGraphData").and.returnValue({
            xAxis: [1.8],
            yAxis: ["Domain A"],
            unitImpact: [1.3],
            subdomainCount: 1,
            appCount: 1,
            clusterList: ["cluster A"],
            equipmentList: ["Laptop"],
            environnementList: ["Production"],
        });

        let result = component.computeData(component.footprint);

        expect(result).toEqual(expectedResult);
        expect(component.computeImpactOrder).toHaveBeenCalledOnceWith(
            callParameters4ComputeImpactOrder,
            callParameters4ComputeImpactOrder.domain,
        );
        expect(component.initGraphData).toHaveBeenCalledOnceWith(component.impactOrder);
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
