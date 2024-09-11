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
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { CardModule } from "primeng/card";
import { TooltipModule } from "primeng/tooltip";
import { Constants } from "src/constants";
import { CriteriaStatsComponent } from "./criteria-stats.component";

describe("CriteriaStatsComponent", () => {
    let component: CriteriaStatsComponent;
    let httpMock: HttpTestingController;
    let fixture: ComponentFixture<CriteriaStatsComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [CriteriaStatsComponent],
            imports: [
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                CardModule,
                TooltipModule,
            ],
            providers: [TranslatePipe, TranslateService],
        });
        fixture = TestBed.createComponent(CriteriaStatsComponent);
        component = fixture.componentInstance;
        httpMock = TestBed.inject(HttpTestingController);
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should compute stats on application graph level", () => {
        component.criteriaFootprint = [
            {
                criteria: "climate-change",
                criteriaTitle: "Climate change",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        lifeCycle: "Using",
                        vmName: "virtual-eq-1",
                        cluster: "PY1LNX02",
                        environment: "Test",
                        equipmentType: "Personal Computer",
                        impact: 1.3,
                        sip: 7.6,
                    },
                    {
                        lifeCycle: "Manufacturing",
                        vmName: "virtual-eq-2",
                        cluster: "PY1LNX02",
                        environment: "Test",
                        equipmentType: "Personal Computer",
                        impact: 98.1,
                        sip: 1.2,
                    },
                ],
            },
        ];
        component.selectedEnvironnementFilter = [Constants.ALL, "Test", "Preproduction"];
        component.selectedEquipmentsFilter = [Constants.ALL, "Personal Computer"];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedCriteriaUri = "climate-change";
        component.computeApplicationStatsAppGraph();

        expect(component.averageImpactSip.toFixed(1)).toEqual("8.8");
        expect(component.averageImpactUnit.toFixed(1)).toEqual("99.4");
    });

    it("should compute stats on application graph level when no data selected", () => {
        component.criteriaFootprint = [
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiation",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        lifeCycle: "Using",
                        vmName: "virtual-eq-1",
                        cluster: "PY1LNX02",
                        environment: "Test",
                        equipmentType: "Personal Computer",
                        impact: 1.3,
                        sip: 7.6,
                    },
                    {
                        lifeCycle: "Manufacturing",
                        vmName: "virtual-eq-2",
                        cluster: "PY1LNX02",
                        environment: "Test",
                        equipmentType: "Personal Computer",
                        impact: 98.1,
                        sip: 1.2,
                    },
                ],
            },
        ];
        component.selectedEnvironnementFilter = [Constants.ALL, "Test", "Preproduction"];
        component.selectedEquipmentsFilter = [Constants.ALL, "Personal Computer"];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedCriteriaUri = "climate-change";

        component.computeApplicationStatsAppGraph();

        expect(component.averageImpactSip).toEqual(0);
        expect(component.averageImpactUnit).toEqual(0);
    });

    it("should compute stats on global graph level", () => {
        component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1b",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "climate-change",
                criteriaTitle: "Climate change",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        applicationName: "App C",
                        domain: "Domain C",
                        subDomain: "subdomain 2c",
                        environment: "Recette",
                        equipmentType: "Communication Device",
                        lifeCycle: "End of life",
                        impact: 1.5,
                        sip: 2.1,
                    },
                ],
            },
        ];
        component.selectedEnvironnementFilter = [
            Constants.ALL,
            "Test",
            "Production",
            "Preproduction",
        ];
        component.selectedDomainFilter = [
            Constants.ALL,
            "Domain A",
            "Domain B",
            "Domain C",
            "Domain D",
        ];
        component.selectedSubDomainFilter = [
            "subdomain 1a",
            "subdomain 2d",
            "subdomain 1b",
            "subdomain 2c",
        ];
        component.selectedEquipmentsFilter = [
            Constants.ALL,
            "Personal Computer",
            "Laptop",
            "Communication Device",
        ];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedCriteriaUri = "acidification";
        component.selectedGraph = "global";

        component.computeApplicationStats();

        expect(component.averageImpactSip.toFixed(1)).toEqual("3.6");
        expect(component.averageImpactUnit.toFixed(1)).toEqual("2.6");
    });

    it("should compute stats on domain graph level", () => {
        component.footprint = component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1b",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1a",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "climate-change",
                criteriaTitle: "Climate change",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        applicationName: "App C",
                        domain: "Domain C",
                        subDomain: "subdomain 2c",
                        environment: "Recette",
                        equipmentType: "Communication Device",
                        lifeCycle: "End of life",
                        impact: 1.5,
                        sip: 2.1,
                    },
                ],
            },
        ];
        component.selectedEnvironnementFilter = [
            Constants.ALL,
            "Test",
            "Production",
            "Preproduction",
        ];
        component.selectedDomainFilter = [
            Constants.ALL,
            "Domain A",
            "Domain B",
            "Domain C",
            "Domain D",
        ];
        component.selectedDomain = "Domain A";
        component.selectedSubDomainFilter = [
            "subdomain 1a",
            "subdomain 2d",
            "subdomain 1b",
            "subdomain 2c",
        ];
        component.selectedEquipmentsFilter = [
            Constants.ALL,
            "Personal Computer",
            "Laptop",
            "Communication Device",
        ];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedCriteriaUri = "acidification";
        component.selectedGraph = "domain";

        component.computeApplicationStats();

        expect(component.averageImpactSip.toFixed(1)).toEqual("3.6");
        expect(component.averageImpactUnit.toFixed(1)).toEqual("2.6");
    });

    it("should compute stats on subdomain graph level", () => {
        component.footprint = component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1b",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1b",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "climate-change",
                criteriaTitle: "Climate change",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        applicationName: "App C",
                        domain: "Domain C",
                        subDomain: "subdomain 2c",
                        environment: "Recette",
                        equipmentType: "Communication Device",
                        lifeCycle: "End of life",
                        impact: 1.5,
                        sip: 2.1,
                    },
                ],
            },
        ];
        component.selectedEnvironnementFilter = [
            Constants.ALL,
            "Test",
            "Production",
            "Preproduction",
        ];
        component.selectedDomainFilter = [
            Constants.ALL,
            "Domain A",
            "Domain B",
            "Domain C",
            "Domain D",
        ];
        component.selectedDomain = "Domain A";
        component.selectedSubdomain = "subdomain 1b";
        component.selectedSubDomainFilter = [
            "subdomain 1a",
            "subdomain 2d",
            "subdomain 1b",
            "subdomain 2c",
        ];
        component.selectedEquipmentsFilter = [
            Constants.ALL,
            "Personal Computer",
            "Laptop",
            "Communication Device",
        ];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedCriteriaUri = "acidification";
        component.selectedGraph = "subdomain";

        component.computeApplicationStats();

        expect(component.averageImpactSip.toFixed(1)).toEqual("3.6");
        expect(component.averageImpactUnit.toFixed(1)).toEqual("2.6");
    });

    it("should compute stats on subdomain graph level", () => {
        component.footprint = component.footprint = [
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1b",
                        environment: "Production",
                        equipmentType: "Laptop",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                    {
                        applicationName: "App A",
                        domain: "Domain A",
                        subDomain: "subdomain 1b",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Manufacturing",
                        impact: 1.3,
                        sip: 1.8,
                    },
                ],
            },
            {
                criteria: "climate-change",
                criteriaTitle: "Climate change",
                unit: "kg CO2 eq",
                impacts: [
                    {
                        applicationName: "App C",
                        domain: "Domain C",
                        subDomain: "subdomain 2c",
                        environment: "Recette",
                        equipmentType: "Communication Device",
                        lifeCycle: "End of life",
                        impact: 1.5,
                        sip: 2.1,
                    },
                ],
            },
        ];
        component.selectedEnvironnementFilter = [];
        component.selectedDomainFilter = [];
        component.selectedDomain = "Domain A";
        component.selectedSubdomain = "subdomain 1b";
        component.selectedSubDomainFilter = [];
        component.selectedEquipmentsFilter = [];
        component.selectedLifecycleFilter = [Constants.ALL, "Using", "Manufacturing"];
        component.selectedCriteriaUri = "acidification";
        component.selectedGraph = "subdomain";

        component.computeApplicationStats();

        expect(component.averageImpactSip).toEqual(0);
        expect(component.averageImpactUnit).toEqual(0);
    });
});
