/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { FormBuilder, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { Router, Routes } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService, SharedModule } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DividerModule } from "primeng/divider";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { RadioButtonModule } from "primeng/radiobutton";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesServersComponent } from "../digital-services-servers.component";
import { SidePanelServerParametersComponent } from "../side-panel-server-parameters/side-panel-server-parameters.component";
import { SidePanelCreateServerComponent } from "./side-panel-create-server.component";

const routes: Routes = [
    {
        path: "",
        component: DigitalServicesServersComponent,
        children: [
            {
                path: "parameters",
                component: SidePanelServerParametersComponent,
            },
        ],
    },
];

describe("SidePanelCreateServerComponent", () => {
    let component: SidePanelCreateServerComponent;
    let fixture: ComponentFixture<SidePanelCreateServerComponent>;
    let serviceBusiness: DigitalServiceBusinessService;
    let router: Router;

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [
                DigitalServicesServersComponent,
                SidePanelCreateServerComponent,
            ],
            imports: [
                FormsModule,
                DividerModule,
                RadioButtonModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputTextModule,
                InputNumberModule,
                ButtonModule,
                SharedModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                RouterTestingModule.withRoutes(routes),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                DigitalServiceBusinessService,
                FormBuilder,
            ],
        });
        serviceBusiness = TestBed.inject(DigitalServiceBusinessService);
        router = TestBed.inject(Router);
        fixture = TestBed.createComponent(SidePanelCreateServerComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should navigate to parameters", () => {
        //spy
        const navigateSpy = spyOn(router, "navigate");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");
        var server = component.server;

        //form
        component.serverForm.controls["name"].setValue("name");
        component.serverForm.controls["mutualizationType"].setValue("Dedicated");
        component.serverForm.controls["type"].setValue("Compute");

        //call
        component.nextStep();

        fixture.detectChanges();
        expect(server.name).toEqual(component.serverForm.value.name!);
        expect(server.name).toEqual(component.serverForm.value.name!);
        expect(server.name).toEqual(component.serverForm.value.name!);
        expect(setServerSpy).toHaveBeenCalled();
        expect(navigateSpy).toHaveBeenCalled();
    });

    it("should close panel", () => {
        const close = spyOn(serviceBusiness, "closePanel");
        serviceBusiness.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        component.close();

        expect(close).toHaveBeenCalled();
    });
});
