/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CommonModule } from "@angular/common";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { CheckboxModule } from "primeng/checkbox";
import { OverlayModule } from "primeng/overlay";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { TabViewModule } from "primeng/tabview";
import { TreeSelectModule } from "primeng/treeselect";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { FilterRepository } from "src/app/core/store/filter.repository";
import { Constants } from "src/constants";
import { DatavizFilterApplicationComponent } from "./dataviz-filter-application.component";

describe("DatavizApplicationFilterComponent", () => {
    let component: DatavizFilterApplicationComponent;
    let fixture: ComponentFixture<DatavizFilterApplicationComponent>;
    let filterRepo: FilterRepository;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DatavizFilterApplicationComponent],
            imports: [
                CommonModule,
                FormsModule,
                ReactiveFormsModule,
                OverlayModule,
                TabViewModule,
                ScrollPanelModule,
                CheckboxModule,
                TreeSelectModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                FootprintDataService,
                FilterRepository,
            ],
        }).compileComponents();
    });
    beforeEach(() => {
        fixture = TestBed.createComponent(DatavizFilterApplicationComponent);
        component = fixture.componentInstance;
        filterRepo = TestBed.inject(FilterRepository);
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should get All application filters", () => {
        spyOn(filterRepo.allApplicationFilters$, "subscribe");

        component.ngOnInit();

        expect(filterRepo.allApplicationFilters$.subscribe).toHaveBeenCalled();
    });

    it("should select all domain's filters", () => {
        spyOn(component, "fillSelectedDomainAndSubDomain");
        spyOn(component, "saveFilters");
        component.filters = {
            domains: [Constants.ALL, "Domain A,subdomain 1a", "Domain B,subdomain 1b"],
            environments: [Constants.ALL, Constants.EMPTY, "Preproduction"],
            types: [Constants.ALL, "Communication Device"],
            lifeCycles: [Constants.ALL, "Distribution"],
        };

        component.allDomainFiltersSelected();

        //Expected selectedObjectDomain
        let expectedSelectedObjectDomain = [
            {
                label: Constants.ALL,
                children: [],
            },
            {
                label: "Domain A",
                children: [
                    {
                        label: "subdomain 1a",
                        domain: "Domain A",
                    },
                ],
            },
            {
                label: "Domain B",
                children: [
                    {
                        label: "subdomain 1b",
                        domain: "Domain B",
                    },
                ],
            },
        ];
        //Expected selectedObjectSuDomain
        let expectedSelectedObjectSubDomain = [
            {
                label: "subdomain 1a",
                domain: "Domain A",
            },
            {
                label: "subdomain 1b",
                domain: "Domain B",
            },
        ];

        let expectedSelectedValuesFilterDomain = [
            {
                label: Constants.ALL,
                children: [],
            },
            {
                label: "Domain A",
                children: [
                    {
                        label: "subdomain 1a",
                        domain: "Domain A",
                    },
                ],
            },
            {
                label: "Domain B",
                children: [
                    {
                        label: "subdomain 1b",
                        domain: "Domain B",
                    },
                ],
            },
            {
                label: "subdomain 1a",
                domain: "Domain A",
            },
            {
                label: "subdomain 1b",
                domain: "Domain B",
            },
        ];

        expect(component.selectedObjectDomain).toEqual(expectedSelectedObjectDomain);
        expect(component.selectedObjectSubDomain).toEqual(
            expectedSelectedObjectSubDomain,
        );
        expect(component.selectedValuesFilterDomain).toEqual(
            expectedSelectedValuesFilterDomain,
        );
    });

    it("should choose the right filter and call updateSelectedValue for environnement", () => {
        spyOn(component, "updateSelectedValues");
        spyOn(component, "saveFilters");
        component.selectedEnvironnement = [
            Constants.ALL,
            Constants.EMPTY,
            "Preproduction",
        ];
        component.filters.environments = [
            Constants.ALL,
            Constants.EMPTY,
            "Preproduction",
        ];

        component.onFilterUpdate("environnement", "Preproduction");

        expect(component.updateSelectedValues).toHaveBeenCalled();
        expect(component.saveFilters).toHaveBeenCalled();
    });

    it("should choose the right filter and call updateSelectedValue for equipment", () => {
        spyOn(component, "updateSelectedValues");
        spyOn(component, "saveFilters");
        component.selectedEquipment = [Constants.ALL, "Communication Device"];
        component.filters.types = [Constants.ALL, "Communication Device"];

        component.onFilterUpdate("equipment", "Communication Device");
        expect(component.updateSelectedValues).toHaveBeenCalled();
        expect(component.saveFilters).toHaveBeenCalled();
    });

    it("should choose the right filter and call updateSelectedValue for lifecycle", () => {
        spyOn(component, "updateSelectedValues");
        spyOn(component, "saveFilters");
        component.selectedlifecycle = [Constants.ALL, "Distribution"];
        component.filters.lifeCycles = [Constants.ALL, "Distribution"];

        component.onFilterUpdate("lifecycle", "Distribution");

        expect(component.updateSelectedValues).toHaveBeenCalled();
        expect(component.saveFilters).toHaveBeenCalled();
    });

    it("should choose the right filter and call updateSelectedValue for default value", () => {
        expect(component.onFilterUpdate("default", "")).toBeUndefined();
    });

    it("should fill selectedDomain and selectedSubdomain", () => {
        component.selectedValuesFilterDomain = [
            {
                label: Constants.ALL,
                children: [],
            },
            {
                label: "Domain A",
                children: [
                    {
                        label: "subdomain 1a",
                        domain: "Domain A",
                    },
                ],
            },
            {
                label: "Domain B",
                children: [
                    {
                        label: "subdomain 1b",
                        domain: "Domain B",
                    },
                ],
            },
            {
                label: "subdomain 1a",
                domain: "Domain A",
            },
            {
                label: "subdomain 1b",
                domain: "Domain B",
            },
        ];

        component.fillSelectedDomainAndSubDomain();

        expect(component.selectedDomain).toEqual([Constants.ALL, "Domain A", "Domain B"]);
        expect(component.selectedSubdomain).toEqual(["subdomain 1a", "subdomain 1b"]);
    });

    it("should choose the right filter and call updateSelectedValue for default value", () => {
        expect(component.onFilterUpdate("default", "")).toBeUndefined();
    });

    it("should add the All value if all is selected", () => {
        component.selectedDomain = ["Domain A", "Domain B"];
        component.selectedValuesFilterDomain = [
            {
                label: "Domain A",
                children: [
                    {
                        label: "subdomain A",
                    },
                ],
            },
            {
                label: "Domain B",
                children: [
                    {
                        label: "subdomain B",
                    },
                ],
            },
        ];
        component.domains = [
            {
                label: Constants.ALL,
                children: [],
            },
        ];
        component.selectedSubdomain.length = 3;
        component.selectedObjectSubDomain.length = 3;
        component.selectedObjectDomain.length = 3;

        component.addAllFilterValue();

        expect(component.selectedDomain.length).toEqual(3);
        expect(component.selectedDomain).toEqual([Constants.ALL, "Domain A", "Domain B"]);
        expect(component.selectedValuesFilterDomain).toEqual([
            {
                label: Constants.ALL,
                children: [],
            },
            {
                label: "Domain A",
                children: [
                    {
                        label: "subdomain A",
                    },
                ],
            },
            {
                label: "Domain B",
                children: [
                    {
                        label: "subdomain B",
                    },
                ],
            },
        ]);
        expect(component.selectedValuesFilterDomain.length).toEqual(3);
    });

    it("should choose the right filter and call updateSelectedValue for default value", () => {
        expect(component.onFilterUpdate("default", "")).toBeUndefined();
    });

    it("should save filter and call updateSelectedFiltersApp", () => {
        component.selectedEnvironnement = [
            Constants.ALL,
            Constants.EMPTY,
            "Preproduction",
        ];
        component.selectedEquipment = [Constants.ALL, "Communication Device"];
        component.selectedlifecycle = [Constants.ALL, "Distribution"];
        component.selectedDomain = [Constants.ALL, "Domain 1", "Domain 2"];
        component.selectedSubdomain = ["subdomain1", "subdomain2", "subdomain3"];
        spyOn(filterRepo, "updateSelectedFiltersApp");

        component.saveFilters();

        expect(filterRepo.updateSelectedFiltersApp).toHaveBeenCalledOnceWith({
            environments: component.selectedEnvironnement,
            types: component.selectedEquipment,
            lifeCycles: component.selectedlifecycle,
            domains: component.selectedDomain,
            subdomains: component.selectedSubdomain,
        });
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
