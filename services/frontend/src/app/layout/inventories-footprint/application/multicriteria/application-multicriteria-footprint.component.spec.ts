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
import { Router, Routes } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { SharedModule } from "src/app/core/shared/shared.module";
import { FilterRepository } from "src/app/core/store/filter.repository";
import { ApplicationCriteriaFootprintComponent } from "../criteria/application-criteria-footprint.component";
import { InventoriesApplicationFootprintComponent } from "../inventories-application-footprint.component";
import { ApplicationMulticriteriaFootprintComponent } from "./application-multicriteria-footprint.component";

const routes: Routes = [
    {
        path: "",
        component: ApplicationMulticriteriaFootprintComponent,
        children: [
            {
                path: "particulate-matter",
                component: ApplicationCriteriaFootprintComponent,
            },
            {
                path: "acidification",
                component: ApplicationCriteriaFootprintComponent,
            },
            {
                path: "resource-use",
                component: ApplicationCriteriaFootprintComponent,
            },
            {
                path: "climate-change",
                component: ApplicationCriteriaFootprintComponent,
            },
            {
                path: "ionising-radiation",
                component: ApplicationCriteriaFootprintComponent,
            },
        ],
    },
];

describe("ApplicationMulticriteriaFootprintComponent", () => {
    let component: ApplicationMulticriteriaFootprintComponent;
    let fixture: ComponentFixture<ApplicationMulticriteriaFootprintComponent>;
    let filterRepo: FilterRepository;
    let router: Router;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [ApplicationMulticriteriaFootprintComponent],
            imports: [
                HttpClientTestingModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
                RouterTestingModule.withRoutes(routes),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                InventoriesApplicationFootprintComponent,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();
        fixture = TestBed.createComponent(ApplicationMulticriteriaFootprintComponent);

        component = fixture.componentInstance;
        filterRepo = TestBed.inject(FilterRepository);
        router = TestBed.inject(Router);
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should init domain filter", () => {
        component.domainFilter = ["All", "Domain A,subdomain A", "Domain B,subdomain B"];

        component.initDomainFilter();

        expect(component.selectedDomain).toEqual(["All", "Domain A", "Domain B"]);
        expect(component.selectedSubDomain).toEqual(["subdomain A", "subdomain B"]);
    });

    it("should check if footprint has data and return no Data", () => {
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
                criteria: "climate-change",
                criteriaTitle: "Climate Change",
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
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiation",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 1b",
                        environment: "Preproduction",
                        equipmentType: "Communication Device",
                        lifeCycle: "Manufacturing",
                        impact: 0.03,
                        sip: 0.08,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource Use",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App D",
                        domain: "Domain D",
                        subDomain: "subdomain 2d",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Distribution",
                        impact: 1.7,
                        sip: 1.2,
                    },
                ],
            },
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
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
        ];
        component.selectedEnvironnement = [
            "All",
            "Production",
            "Preproduction",
            "Recette",
        ];
        component.selectedEquipments = ["All", "Laptop"];
        component.selectedLifecycle = [
            "All",
            "Distribution",
            "manufacturing",
            "Using",
            "End of life",
        ];
        component.selectedDomain = [
            "All",
            "Domain A",
            "Domain B",
            "Domain C",
            "Domain D",
        ];
        component.selectedSubDomain = [
            "subdomain 1a",
            "subdomain 2d",
            "subdomain 1b",
            "subdomain 2c",
        ];

        var result = component.checkIfNoData();

        expect(result).toBe(true);
    });

    it("should check if footprint has data and return has Data", () => {
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
                criteria: "climate-change",
                criteriaTitle: "Climate Change",
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
            {
                criteria: "ionising-radiation",
                criteriaTitle: "Ionising Radiation",
                unit: "kBq U-235 eq",
                impacts: [
                    {
                        applicationName: "App B",
                        domain: "Domain B",
                        subDomain: "subdomain 1b",
                        environment: "Preproduction",
                        equipmentType: "Communication Device",
                        lifeCycle: "Manufacturing",
                        impact: 0.03,
                        sip: 0.08,
                    },
                ],
            },
            {
                criteria: "resource-use",
                criteriaTitle: "Resource Use",
                unit: "kg Sb eq",
                impacts: [
                    {
                        applicationName: "App D",
                        domain: "Domain D",
                        subDomain: "subdomain 2d",
                        environment: "Production",
                        equipmentType: "Communication Device",
                        lifeCycle: "Distribution",
                        impact: 1.7,
                        sip: 1.2,
                    },
                ],
            },
            {
                criteria: "acidification",
                criteriaTitle: "Acidification",
                unit: "mol H+ eq",
                impacts: [
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
        ];
        component.selectedEnvironnement = [
            "All",
            "Production",
            "Preproduction",
            "Recette",
        ];
        component.selectedEquipments = ["All", "Laptop", "Communication Device"];
        component.selectedLifecycle = [
            "All",
            "Distribution",
            "manufacturing",
            "Using",
            "End of life",
        ];
        component.selectedDomain = [
            "All",
            "Domain A",
            "Domain B",
            "Domain C",
            "Domain D",
        ];
        component.selectedSubDomain = [
            "subdomain 1a",
            "subdomain 2d",
            "subdomain 1b",
            "subdomain 2c",
        ];

        var result = component.checkIfNoData();

        expect(result).toBe(false);
    });

    it("should get uri from criterias lang object", () => {
        const translatedCriteria = {
            "multi-criteria": {
                title: "Multi-criteria",
            },
            "particulate-matter": {
                title: "Particulate matter",
                name: "Particulate matter",
                unite: "Disease incidence",
            },
        };

        const uri = component.getUriFromCriterias(
            translatedCriteria,
            "Particulate matter"
        );

        expect(uri).toEqual("particulate-matter");
    });
});
