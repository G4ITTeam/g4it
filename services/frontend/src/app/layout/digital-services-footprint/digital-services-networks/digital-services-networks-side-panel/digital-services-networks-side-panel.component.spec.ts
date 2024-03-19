/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { forwardRef } from "@angular/core";
import { FormBuilder, NG_VALUE_ACCESSOR, ReactiveFormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel.component";
import { UserService } from "src/app/core/service/business/user.service";
import { MessageService } from "primeng/api";

describe("DigitalServicesNetworksSidePanelComponent", () => {
    let component: DigitalServicesNetworksSidePanelComponent;
    let fixture: ComponentFixture<DigitalServicesNetworksSidePanelComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [DigitalServicesNetworksSidePanelComponent],
            providers: [
                {
                    provide: NG_VALUE_ACCESSOR,
                    useExisting: forwardRef(
                        () => DigitalServicesNetworksSidePanelComponent
                    ),
                    multi: true,
                },
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                DigitalServicesDataService,
                FormBuilder,
                NgxSpinnerService,
            ],
            imports: [
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputNumberModule,
                InputTextModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
        });
        fixture = TestBed.createComponent(DigitalServicesNetworksSidePanelComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("it should close", () => {
        spyOn(component.sidebarVisibleChange, "emit");
        component.close();
        expect(component.sidebarVisibleChange.emit).toHaveBeenCalled();
    });

    it("should delete terminal", () => {
        spyOn(component.deleteNetworks, "emit");
        component.deleteNetwork();
        expect(component.deleteNetworks.emit).toHaveBeenCalled();
    });

    it("should submit data ", () => {
        spyOn(component.updateNetworks, "emit");
        component.networksForm.controls["type"].setValue({
            code: "mobile",
            value: "mobile Fr",
        });
        component.submitFormData();
        expect(component.network.type.code).toEqual("mobile");
        expect(component.updateNetworks.emit).toHaveBeenCalled();
        expect(component.close()).toHaveBeenCalled;
    });
});
