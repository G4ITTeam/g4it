/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";
import { FormBuilder, FormsModule, ReactiveFormsModule } from "@angular/forms";
import { Router, Routes } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService, SharedModule } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { of } from "rxjs";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesServersComponent } from "../digital-services-servers.component";
import { SidePanelAddVmComponent } from "../side-panel-add-vm/side-panel-add-vm.component";
import { SidePanelServerParametersComponent } from "../side-panel-server-parameters/side-panel-server-parameters.component";
import { SidePanelListVmComponent } from "./side-panel-list-vm.component";
import { UserService } from "src/app/core/service/business/user.service";

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

describe("SidePanelListVmComponent", () => {
    let component: SidePanelListVmComponent;
    let fixture: ComponentFixture<SidePanelListVmComponent>;
    let ServiceBusiness: DigitalServiceBusinessService;
    let serviceData: DigitalServicesDataService;
    let spinner: NgxSpinnerService;
    let router: Router;

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [SidePanelListVmComponent, SidePanelAddVmComponent],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputTextModule,
                InputNumberModule,
                ButtonModule,
                SharedModule,
                TableModule,
                SidebarModule,
                HttpClientTestingModule,
                RouterTestingModule.withRoutes(routes),
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                DigitalServiceBusinessService,
                DigitalServicesDataService,
                NgxSpinnerService,
                FormBuilder,
            ],
        });
        ServiceBusiness = TestBed.inject(DigitalServiceBusinessService);
        serviceData = TestBed.inject(DigitalServicesDataService);
        spinner = TestBed.inject(NgxSpinnerService);
        router = TestBed.inject(Router);
        fixture = TestBed.createComponent(SidePanelListVmComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should reset vm when resetIndex() is call", () => {
        //call function
        component.resetIndex();
        expect(component.index).toEqual(undefined);
        expect(component.addVMPanelVisible).toBeTrue();
    });

    it("should set vm when setIndex() is call", () => {
        //call function
        component.setIndex(0);
        expect(component.index).toEqual(0);
        expect(component.addVMPanelVisible).toBeTrue();
    });

    it("should delete vm", () => {
        //spy and mock data
        const show = spyOn(spinner, "show");
        const hide = spyOn(spinner, "hide");
        var server = component.server;
        server.vm = [
            {
                uid: "uid",
                name: "name ",
                vCpu: 14,
                disk: 3,
                quantity: 11,
                annualOperatingTime: 12000,
            },
        ];

        //call
        component.deleteVm(0);

        //expectation
        fixture.detectChanges();
        expect(server.vm).toHaveSize(0);
        expect(show).toHaveBeenCalled();
        expect(hide).toHaveBeenCalled();
    });

    it("should not delete vm", () => {
        //spy and mock data
        const show = spyOn(spinner, "show");
        const hide = spyOn(spinner, "hide");
        var server = component.server;
        server.vm = [
            {
                uid: "uid",
                name: "name ",
                vCpu: 14,
                disk: 3,
                quantity: 11,
                annualOperatingTime: 12000,
            },
        ];

        //call
        component.deleteVm(1);

        //expectation
        fixture.detectChanges();
        expect(server.vm).toHaveSize(1);
        expect(show).toHaveBeenCalled();
        expect(hide).toHaveBeenCalled();
    });

    it("should navigate to parameters when click on previous", () => {
        //spy
        const navigateSpy = spyOn(router, "navigate");

        //call
        component.previousStep();

        //expectation
        expect(navigateSpy).toHaveBeenCalled();
    });

    it("should create new server in service when click on create", fakeAsync(() => {
        //spy and mock data
        const update = spyOn(serviceData, "update").and.returnValue(
            of({
                name: "name",
                uid: "uid",
                creationDate: Date.now(),
                lastUpdateDate: Date.now(),
                lastCalculationDate: null,
                terminals: [],
                servers: [],
                networks: [],
            })
        );
        const setServerSpy = spyOn(ServiceBusiness, "setServerForm");
        const show = spyOn(spinner, "show");
        const hide = spyOn(spinner, "hide");
        const close = spyOn(component, "close");
        var server = component.server;
        server.uid = "uid";
        var digitalService = component.digitalService;
        digitalService.servers = [];

        //call
        component.submitServer();
        tick();

        //expectation
        expect(setServerSpy).toHaveBeenCalledWith(server);
        expect(show).toHaveBeenCalled();
        expect(hide).toHaveBeenCalled();
        expect(close).toHaveBeenCalled();
        expect(update).toHaveBeenCalledWith(digitalService);
        expect(digitalService.servers).toHaveSize(1);
    }));

    it("should update server in service when Dedicated and click on next", fakeAsync(() => {
        //spy and mock data
        const update = spyOn(serviceData, "update").and.returnValue(
            of({
                name: "name",
                uid: "uid",
                creationDate: Date.now(),
                lastUpdateDate: Date.now(),
                lastCalculationDate: null,
                terminals: [],
                servers: [],
                networks: [],
            })
        );
        const setServerSpy = spyOn(ServiceBusiness, "setServerForm");
        const show = spyOn(spinner, "show");
        const hide = spyOn(spinner, "hide");
        const close = spyOn(component, "close");
        var server = component.server;
        server.uid = "uid";
        var digitalService = component.digitalService;
        digitalService.servers = [server];

        //call
        component.submitServer();
        tick();

        //expectation
        expect(setServerSpy).toHaveBeenCalledWith(server);
        expect(show).toHaveBeenCalled();
        expect(hide).toHaveBeenCalled();
        expect(close).toHaveBeenCalled();
        expect(update).toHaveBeenCalledWith(digitalService);
        expect(digitalService.servers).toHaveSize(1);
    }));

    it("should close panel", () => {
        const close = spyOn(ServiceBusiness, "closePanel");
        ServiceBusiness.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        component.close();

        expect(close).toHaveBeenCalled();
    });

    it("should open panel", () => {
        const open = spyOn(ServiceBusiness, "openPanel");
        ServiceBusiness.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(true);
        });

        component.openSidePanel();

        expect(open).toHaveBeenCalled();
    });
});
