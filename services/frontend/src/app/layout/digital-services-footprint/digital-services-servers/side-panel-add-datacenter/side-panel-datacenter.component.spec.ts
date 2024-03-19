/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { ButtonModule } from "primeng/button";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { FormBuilder, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService, SharedModule } from "primeng/api";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { of } from "rxjs";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import SidePanelDatacenterComponent from "./side-panel-datacenter.component";
import { UserService } from "src/app/core/service/business/user.service";

describe("SidePanelDatacenterComponent", () => {
    let component: SidePanelDatacenterComponent;
    let fixture: ComponentFixture<SidePanelDatacenterComponent>;
    let serviceData: DigitalServicesDataService;

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [SidePanelDatacenterComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputTextModule,
                InputNumberModule,
                ButtonModule,
                SharedModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                DigitalServicesDataService,
                NgxSpinnerService,
                FormBuilder,
            ],
        });
        serviceData = TestBed.inject(DigitalServicesDataService);
        fixture = TestBed.createComponent(SidePanelDatacenterComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should get country referentials for server when getCountryReferential() is called", fakeAsync(() => {
        //Mock call of referential
        const countriesRef = spyOn(serviceData, "getCountryReferential").and.returnValue(
            of(["France", "Germany", "China"])
        );

        //Call
        component.getCountryReferential();
        tick();

        const countries = component.countries;

        //expectations
        expect(countriesRef).toHaveBeenCalled();
        expect(countries).toHaveSize(3);
        expect(countries[1]).toEqual({
            value: "France",
            label: "France",
        });
        expect(countries[2]).toEqual({
            value: "Germany",
            label: "Germany",
        });
        expect(countries[0]).toEqual({
            value: "China",
            label: "China",
        });
    }));

    it("isToLow should be false", () => {
        //form
        component.datacenterForm.controls["pue"].setValue(1);

        //call and expectation
        component.verifyPue();

        fixture.detectChanges();
        const isToLow = component.isToLow;
        expect(isToLow).toBeFalse();
    });

    it("isToLow should be true", () => {
        //form
        component.datacenterForm.controls["pue"].setValue(0.9);

        //call and expectation
        component.verifyPue();

        fixture.detectChanges();
        const isToLow = component.isToLow;
        expect(isToLow).toBeTrue();
    });

    it("should submit form data", () => {
        //spy
        const close = spyOn(component, "close");
        const event = spyOn(component.serverChange, "emit");

        var isToLow = component.isToLow;
        isToLow = false;
        var server = component.server;

        let datacenterMock = {
            uid: "",
            name: "name",
            location: "France",
            pue: 5,
        };

        //form
        component.datacenterForm.controls["name"].setValue("name");
        component.datacenterForm.controls["country"].setValue("France");
        component.datacenterForm.controls["pue"].setValue(5);

        //call and expectation
        component.submitFormData();
        fixture.detectChanges();
        server = component.server;
        expect(close).toHaveBeenCalled();
        expect(event).toHaveBeenCalledWith(datacenterMock);
    });

    it("should not submit form data", () => {
        var isToLow = component.isToLow;
        isToLow = true;

        component.submitFormData();
        fixture.detectChanges();
        expect().nothing();
    });

    it("should close panel", () => {
        const close = spyOn(component.addSidebarVisibleChange, "emit");

        component.close();

        expect(close).toHaveBeenCalledWith(false);
    });
});
