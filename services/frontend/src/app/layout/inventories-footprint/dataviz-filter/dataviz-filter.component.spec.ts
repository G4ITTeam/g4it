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
import { Subject } from "rxjs";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { Filter, FilterRepository } from "src/app/core/store/filter.repository";
import { DatavizFilterComponent } from "./dataviz-filter.component";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";

describe("DatavizFilterComponent", () => {
    let component: DatavizFilterComponent;
    let fixture: ComponentFixture<DatavizFilterComponent>;

    const mockFilterRepository = {
        allFilters$: new Subject<Filter>(),
        updateSelectedFilters: jasmine.createSpy(),
    };

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DatavizFilterComponent],
            imports: [
                CommonModule,
                FormsModule,
                ReactiveFormsModule,
                OverlayModule,
                TabViewModule,
                ScrollPanelModule,
                CheckboxModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                FootprintDataService,
                { provide: FilterRepository, useValue: mockFilterRepository },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();
    });
    beforeEach(() => {
        fixture = TestBed.createComponent(DatavizFilterComponent);
        component = fixture.componentInstance;
    });

    it("should create the component", () => {
        expect(component).toBeTruthy();
    });

    it("should delete All option when one element is selected", () => {
        const testFilters: Filter = {
            countries: ["All", "France", "England", "Empty"],
            entities: ["All", "Entity A", "Entity B", "Empty"],
            equipments: ["All", "Mobile", "Laptop", "Empty"],
            status: ["All", "Online", "Offline", "Empty"],
        };
        component.filters = testFilters;
        component.selectedCountries = testFilters.countries;
        component.selectedEntities = testFilters.entities;
        component.selectedEquipments = testFilters.equipments;
        component.selectedStatus = testFilters.status;

        component.onFilterUpdate("country", "France");
        expect(component.selectedCountries).not.toContain("All");

        component.onFilterUpdate("entity", "Empty");
        expect(component.selectedEntities).not.toContain("All");

        component.onFilterUpdate("equipment", "Laptop");
        expect(component.selectedEquipments).not.toContain("All");

        component.onFilterUpdate("status", "Online");
        expect(component.selectedStatus).not.toContain("All");

        expect(mockFilterRepository.updateSelectedFilters).toHaveBeenCalledWith({
            countries: ["France", "England", "Empty"],
            entities: ["Entity A", "Entity B", "Empty"],
            equipments: ["Mobile", "Laptop", "Empty"],
            status: ["Online", "Offline", "Empty"],
        });
    });

    it("should select All option when all elements are selected", () => {
        const testFilters: Filter = {
            countries: ["All", "France", "England", "Empty"],
            entities: ["All", "Entity A", "Entity B", "Empty"],
            equipments: ["All", "Mobile", "Laptop", "Empty"],
            status: ["All", "Online", "Offline", "Empty"],
        };
        component.filters = testFilters;
        component.selectedCountries = ["France", "England", "Empty"];
        component.selectedEntities = ["Entity A", "Entity B", "Empty"];
        component.selectedEquipments = ["Mobile", "Laptop", "Empty"];
        component.selectedStatus = ["Online", "Offline", "Empty"];

        component.onFilterUpdate("country", "France");
        expect(component.selectedCountries).toContain("All");

        component.onFilterUpdate("entity", "Empty");
        expect(component.selectedEntities).toContain("All");

        component.onFilterUpdate("equipment", "Laptop");
        expect(component.selectedEquipments).toContain("All");

        component.onFilterUpdate("status", "Online");
        expect(component.selectedStatus).toContain("All");
    });

    it("should unselect everything on All click", () => {
        const testFilters: Filter = {
            countries: ["All", "France", "England", "Empty"],
            entities: ["All", "Entity A", "Entity B", "Empty"],
            equipments: ["All", "Mobile", "Laptop", "Empty"],
            status: ["All", "Online", "Offline", "Empty"],
        };
        component.filters = testFilters;
        component.selectedCountries = ["France", "England", "Empty"];
        component.selectedEntities = ["Entity A", "Entity B", "Empty"];
        component.selectedEquipments = ["Mobile", "Laptop", "Empty"];
        component.selectedStatus = ["Online", "Offline", "Empty"];

        component.onFilterUpdate("country", "All");
        expect(component.selectedCountries).toEqual([]);

        component.onFilterUpdate("entity", "All");
        expect(component.selectedEntities).toEqual([]);

        component.onFilterUpdate("equipment", "All");
        expect(component.selectedEquipments).toEqual([]);

        component.onFilterUpdate("status", "All");
        expect(component.selectedStatus).toEqual([]);
    });

    it("should select everything on All click", () => {
        const testFilters: Filter = {
            countries: ["All", "France", "England", "Empty"],
            entities: ["All", "Entity A", "Entity B", "Empty"],
            equipments: ["All", "Mobile", "Laptop", "Empty"],
            status: ["All", "Online", "Offline", "Empty"],
        };
        component.filters = testFilters;
        component.selectedCountries = ["All"];
        component.selectedEntities = ["All"];
        component.selectedEquipments = ["All"];
        component.selectedStatus = ["All"];

        component.onFilterUpdate("country", "All");
        expect(component.selectedCountries).toEqual(testFilters.countries);

        component.onFilterUpdate("entity", "All");
        expect(component.selectedEntities).toEqual(testFilters.entities);

        component.onFilterUpdate("equipment", "All");
        expect(component.selectedEquipments).toEqual(testFilters.equipments);

        component.onFilterUpdate("status", "All");
        expect(component.selectedStatus).toEqual(testFilters.status);
    });

    it("should toggle overlayVisible property when toggle method is called", () => {
        expect(component.overlayVisible).toBeFalse();
        component.toggle();
        expect(component.overlayVisible).toBeTrue();
        component.toggle();
        expect(component.overlayVisible).toBeFalse();
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
