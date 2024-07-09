/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { Router, Routes } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { of } from "rxjs";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesServersComponent } from "./digital-services-servers.component";
import { SidePanelCreateServerComponent } from "./side-panel-create-server/side-panel-create-server.component";
import { SidePanelServerParametersComponent } from "./side-panel-server-parameters/side-panel-server-parameters.component";

const routes: Routes = [
    {
        path: "",
        component: DigitalServicesServersComponent,
        children: [
            {
                path: "create",
                component: SidePanelCreateServerComponent,
            },
            {
                path: "parameters",
                component: SidePanelServerParametersComponent,
            },
        ],
    },
];

describe("DigitalServicesServersComponent", () => {
    let component: DigitalServicesServersComponent;
    let serviceBusiness: DigitalServiceBusinessService;
    let httpMock: HttpTestingController;
    let serviceData: DigitalServicesDataService;
    let fixture: ComponentFixture<DigitalServicesServersComponent>;
    let router: Router;
    let spinner: NgxSpinnerService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [DigitalServicesServersComponent],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                DigitalServicesDataService,
                FormBuilder,
                NgxSpinnerService,
            ],
            imports: [
                TableModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputNumberModule,
                InputTextModule,
                SidebarModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                RouterTestingModule.withRoutes(routes),
            ],
        });
        spinner = TestBed.inject(NgxSpinnerService);
        serviceBusiness = TestBed.inject(DigitalServiceBusinessService);
        serviceData = TestBed.inject(DigitalServicesDataService);
        httpMock = TestBed.inject(HttpTestingController);
        fixture = TestBed.createComponent(DigitalServicesServersComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        router = TestBed.inject(Router);
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should reset server when addNewServer() is called", () => {
        //SpyOn navigate and setServerForm function
        const navigateSpy = spyOn(router, "navigate");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");
        const open = spyOn(serviceBusiness, "openPanel");
        serviceBusiness.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        //expected server after call
        var expectedServer = {
            uid: "",
            name: "Server A",
            mutualizationType: "Dedicated",
            type: "Compute",
            quantity: 0,
            datacenter: {
                uid: "",
                name: "",
                location: "",
                pue: 0,
            },
            vm: [],
        };

        //call function
        component.addNewServer();
        expect(navigateSpy).toHaveBeenCalled();
        expect(setServerSpy).toHaveBeenCalledWith(expectedServer);
        expect(open).toHaveBeenCalled();
    });

    it("should set server when updateServer() is called", () => {
        //SpyOn navigate and setServerForm function
        const navigateSpy = spyOn(router, "navigate");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");
        const open = spyOn(serviceBusiness, "openPanel");
        serviceBusiness.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        //mock data
        const server = {
            uid: "randomUID",
            name: "Server A",
            mutualizationType: "Dedicated",
            type: "Storage",
            quantity: 3,
            host: {
                code: 1,
                value: "Server Storage M",
                characteristic: [],
            },
            datacenter: {
                uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                name: "Default DC",
                location: "France",
                pue: 1,
            },
            totalVCpu: 100,
            totalDisk: 53,
            lifespan: 10.5,
            annualElectricConsumption: 1000,
            annualOperatingTime: 8760,
            vm: [],
        };

        //call function
        component.updateServer(server);
        const expectedServer = server;
        expect(expectedServer).toEqual(server);
        expect(navigateSpy).toHaveBeenCalled();
        expect(setServerSpy).toHaveBeenCalledWith(expectedServer);
        expect(open).toHaveBeenCalled();
    });

    it("should delete server", fakeAsync(() => {
        //mock data
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
            }),
        );
        const show = spyOn(spinner, "show");
        const hide = spyOn(spinner, "hide");
        const server = {
            uid: "randomUID",
            name: "Server A",
            mutualizationType: "Dedicated",
            type: "Storage",
            quantity: 3,
            host: {
                code: 1,
                value: "Server Storage M",
                characteristic: [],
            },
            datacenter: {
                uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                name: "Default DC",
                location: "France",
                pue: 1,
            },
            totalVCpu: 100,
            totalDisk: 53,
            lifespan: 10.5,
            annualElectricConsumption: 1000,
            annualOperatingTime: 8760,
            vm: [],
        };
        const digitalService = component.digitalService;
        digitalService.servers.push({
            uid: "randomUID",
            name: "Server A",
            mutualizationType: "Dedicated",
            type: "Storage",
            quantity: 3,
            host: {
                code: 1,
                value: "Server Storage M",
                characteristic: [],
            },
            datacenter: {
                uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                name: "Default DC",
                location: "France",
                pue: 1,
            },
            totalVCpu: 100,
            totalDisk: 53,
            lifespan: 10.5,
            annualElectricConsumption: 1000,
            annualOperatingTime: 8760,
            vm: [],
        });

        component.deleteServers(server);
        tick();
        expect(component.digitalService.servers).toHaveSize(0);
        expect(update).toHaveBeenCalledWith(digitalService);
        expect(show).toHaveBeenCalled();
        expect(hide).toHaveBeenCalled();
    }));

    it("should not delete server", fakeAsync(() => {
        //mock data
        const update = spyOn(serviceData, "update").and.returnValue(
            of({
                name: "name",
                uid: "uid",
                creationDate: Date.now(),
                lastUpdateDate: Date.now(),
                lastCalculationDate: null,
                terminals: [],
                servers: [
                    {
                        uid: "randomUID2",
                        name: "Server A",
                        mutualizationType: "Dedicated",
                        type: "Storage",
                        quantity: 3,
                        host: {
                            code: 1,
                            value: "Server Storage M",
                            characteristic: [],
                        },
                        datacenter: {
                            uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                            name: "Default DC",
                            location: "France",
                            pue: 1,
                        },
                        totalVCpu: 100,
                        totalDisk: 53,
                        lifespan: 10.5,
                        annualElectricConsumption: 1000,
                        annualOperatingTime: 8760,
                        vm: [],
                    },
                ],
                networks: [],
            }),
        );
        const show = spyOn(spinner, "show");
        const hide = spyOn(spinner, "hide");
        const server = {
            uid: "randomUID2",
            name: "Server A",
            mutualizationType: "Dedicated",
            type: "Storage",
            quantity: 3,
            host: {
                code: 1,
                value: "Server Storage M",
                characteristic: [],
            },
            datacenter: {
                uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                name: "Default DC",
                location: "France",
                pue: 1,
            },
            totalVCpu: 100,
            totalDisk: 53,
            lifespan: 10.5,
            annualElectricConsumption: 1000,
            annualOperatingTime: 8760,
            vm: [],
        };

        var digitalService = component.digitalService;
        digitalService.servers = [
            {
                uid: "randomUID1",
                name: "Server A",
                mutualizationType: "Dedicated",
                type: "Storage",
                quantity: 3,
                host: {
                    code: 1,
                    value: "Server Storage M",
                    characteristic: [],
                },
                datacenter: {
                    uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
                    name: "Default DC",
                    location: "France",
                    pue: 1,
                },
                totalVCpu: 100,
                totalDisk: 53,
                lifespan: 10.5,
                annualElectricConsumption: 1000,
                annualOperatingTime: 8760,
                vm: [],
            },
        ];

        component.deleteServers(server);
        tick();
        expect(component.digitalService.servers).toHaveSize(1);
        expect(update).toHaveBeenCalledWith(digitalService);
        expect(show).toHaveBeenCalled();
        expect(hide).toHaveBeenCalled();
    }));
});
