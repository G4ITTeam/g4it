/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { ComponentFixture, TestBed, fakeAsync, tick } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { FormBuilder, ReactiveFormsModule } from "@angular/forms";
import { Router, Routes } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService, SharedModule } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { SidebarModule } from "primeng/sidebar";
import { of } from "rxjs";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesServersComponent } from "../digital-services-servers.component";
import SidePanelDatacenterComponent from "../side-panel-add-datacenter/side-panel-datacenter.component";
import { SidePanelCreateServerComponent } from "../side-panel-create-server/side-panel-create-server.component";
import { SidePanelListVmComponent } from "../side-panel-list-vm/side-panel-list-vm.component";
import { SidePanelServerParametersComponent } from "./side-panel-server-parameters.component";

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
                path: "vm",
                component: SidePanelListVmComponent,
            },
        ],
    },
];

describe("SidePanelServerParametersComponent", () => {
    let component: SidePanelServerParametersComponent;
    let fixture: ComponentFixture<SidePanelServerParametersComponent>;
    let serviceBusiness: DigitalServiceBusinessService;
    let serviceData: DigitalServicesDataService;
    let router: Router;

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [
                SidePanelServerParametersComponent,
                SidePanelDatacenterComponent,
            ],
            imports: [
                SidebarModule,
                DropdownModule,
                InputNumberModule,
                HttpClientTestingModule,
                SharedModule,
                ButtonModule,
                RouterTestingModule.withRoutes(routes),
                TranslateModule.forRoot(),
                ReactiveFormsModule,
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                UserService,
                MessageService,
                DigitalServiceBusinessService,
                DigitalServicesDataService,
                FormBuilder,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        serviceBusiness = TestBed.inject(DigitalServiceBusinessService);
        serviceData = TestBed.inject(DigitalServicesDataService);
        fixture = TestBed.createComponent(SidePanelServerParametersComponent);
        router = TestBed.inject(Router);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should get server referentials when getServersReferentials() is called on create when Compute", fakeAsync(() => {
        //Mock call of referential
        const host = spyOn(serviceData, "getHostServerReferential").and.returnValue(
            of([
                {
                    code: 1,
                    value: "Server Compute S",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "vCPU",
                            value: 36,
                        },
                    ],
                },
                {
                    code: 2,
                    value: "Server Compute M",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "vCPU",
                            value: 56,
                        },
                    ],
                },
                {
                    code: 3,
                    value: "Server Compute L",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "vCPU",
                            value: 94,
                        },
                    ],
                },
            ]),
        );
        const datacenter = spyOn(
            serviceData,
            "getDatacenterServerReferential",
        ).and.returnValue(
            of([
                {
                    uid: "",
                    name: "Default DC",
                    location: "France",
                    pue: 1.5,
                    displayLabel: "Default DC (France - PUE = 1.5)",
                },
            ]),
        );
        const initializeDefaultValue = spyOn(component, "initializeDefaultValue");
        var server = component.server;
        server.uid = "";
        server.lifespan = undefined;
        server.type = "Compute";

        //Call
        component.setHostReferential(server.type);
        component.setDatacenterReferential(server.datacenter);
        if (server.uid === "" && !component.dataInitialized) {
            component.initializeDefaultValue();
        }
        tick();

        // get value of variables
        const hostOptions = component.hostOptions;
        const datacenterOptions = component.datacenterOptions;
        const indexHostCompute = component.hostOptions.findIndex(
            (x) => x.value === "Server Compute M",
        );
        server.host = component.hostOptions[indexHostCompute];

        //expectations
        expect(host).toHaveBeenCalled();
        expect(hostOptions[0]).toEqual({
            code: 1,
            value: "Server Compute S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 36,
                },
            ],
        });
        expect(hostOptions[1]).toEqual({
            code: 2,
            value: "Server Compute M",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 56,
                },
            ],
        });
        expect(hostOptions[2]).toEqual({
            code: 3,
            value: "Server Compute L",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 94,
                },
            ],
        });
        expect(hostOptions).toHaveSize(3);
        expect(datacenter).toHaveBeenCalled();
        expect(datacenterOptions).toEqual([
            {
                uid: "",
                name: "Default DC",
                location: "France",
                pue: 1.5,
                displayLabel: "Default DC (France - PUE = 1.5)",
            },
        ]);
        expect(datacenterOptions).toHaveSize(1);
        expect(indexHostCompute).toEqual(1);
        expect(component.server.host).toEqual({
            code: 2,
            value: "Server Compute M",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 56,
                },
            ],
        });
        expect(initializeDefaultValue).toHaveBeenCalled();
    }));

    it("should get server referentials when getServersReferentials() is called on create when Storage", fakeAsync(() => {
        //Mock call of referential
        const host = spyOn(serviceData, "getHostServerReferential").and.returnValue(
            of([
                {
                    code: 1,
                    value: "Server Storage S",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "disk",
                            value: 36,
                        },
                    ],
                },
                {
                    code: 2,
                    value: "Server Storage M",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "disk",
                            value: 56,
                        },
                    ],
                },
            ]),
        );
        const datacenter = spyOn(
            serviceData,
            "getDatacenterServerReferential",
        ).and.returnValue(
            of([
                {
                    uid: "",
                    name: "Default DC",
                    location: "France",
                    pue: 1.5,
                    displayLabel: "Default DC (France - PUE = 1.5)",
                },
            ]),
        );
        const initializeDefaultValue = spyOn(component, "initializeDefaultValue");
        var server = component.server;
        server.type = "Storage";
        server.uid = "";

        //Call
        component.setHostReferential(server.type);
        component.setDatacenterReferential(server.datacenter);
        if (server.uid === "" && !component.dataInitialized) {
            component.initializeDefaultValue();
        }
        tick();

        // get value of variables
        const hostOptions = component.hostOptions;
        const datacenterOptions = component.datacenterOptions;
        const indexHostCompute = component.hostOptions.findIndex(
            (x) => x.value === "Server Storage M",
        );
        server.host = component.hostOptions[indexHostCompute];

        //expectations
        expect(host).toHaveBeenCalled();
        expect(hostOptions[0]).toEqual({
            code: 1,
            value: "Server Storage S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "disk",
                    value: 36,
                },
            ],
        });
        expect(hostOptions[1]).toEqual({
            code: 2,
            value: "Server Storage M",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "disk",
                    value: 56,
                },
            ],
        });
        expect(hostOptions).toHaveSize(2);
        expect(datacenter).toHaveBeenCalled();
        expect(datacenterOptions).toEqual([
            {
                uid: "",
                name: "Default DC",
                location: "France",
                pue: 1.5,
                displayLabel: "Default DC (France - PUE = 1.5)",
            },
        ]);
        expect(datacenterOptions).toHaveSize(1);
        expect(indexHostCompute).toEqual(1);
        expect(component.server.host).toEqual({
            code: 2,
            value: "Server Storage M",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "disk",
                    value: 56,
                },
            ],
        });
        expect(initializeDefaultValue).toHaveBeenCalled();
    }));

    it("should get server referentials when getServersReferentials() is called on update when Compute", fakeAsync(() => {
        //Mock call of referential
        const host = spyOn(serviceData, "getHostServerReferential").and.returnValue(
            of([
                {
                    code: 1,
                    value: "Server Compute S",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "vCPU",
                            value: 36,
                        },
                    ],
                },
                {
                    code: 2,
                    value: "Server Compute M",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "vCPU",
                            value: 56,
                        },
                    ],
                },
            ]),
        );
        const datacenter = spyOn(
            serviceData,
            "getDatacenterServerReferential",
        ).and.returnValue(
            of([
                {
                    uid: "",
                    name: "Default DC",
                    location: "France",
                    pue: 1.5,
                },
            ]),
        );
        var server = component.server;
        server.type = "Compute";
        server.uid = "uid";

        //Call
        component.setHostReferential(server.type);
        component.setDatacenterReferential(server.datacenter);
        if (server.uid === "" && !component.dataInitialized) {
            component.initializeDefaultValue();
        }
        tick();

        // get value of variables
        const hostOptions = component.hostOptions;
        const datacenterOptions = component.datacenterOptions;

        //expectations
        expect(host).toHaveBeenCalled();
        expect(hostOptions[0]).toEqual({
            code: 1,
            value: "Server Compute S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 36,
                },
            ],
        });
        expect(hostOptions[1]).toEqual({
            code: 2,
            value: "Server Compute M",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 56,
                },
            ],
        });
        expect(hostOptions).toHaveSize(2);
        expect(datacenter).toHaveBeenCalled();
        expect(datacenterOptions).toEqual([
            {
                uid: "",
                name: "Default DC",
                location: "France",
                pue: 1.5,
                displayLabel: "Default DC (France - PUE = 1.5)",
            },
        ]);
        expect(datacenterOptions).toHaveSize(1);
    }));

    it("should get server referentials when getServersReferentials() is called on update when Storage", fakeAsync(() => {
        //Mock call of referential
        const host = spyOn(serviceData, "getHostServerReferential").and.returnValue(
            of([
                {
                    code: 1,
                    value: "Server Storage S",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "disk",
                            value: 36,
                        },
                    ],
                },
                {
                    code: 2,
                    value: "Server Storage M",
                    characteristic: [
                        {
                            code: "lifespan",
                            value: 5,
                        },
                        {
                            code: "disk",
                            value: 56,
                        },
                    ],
                },
            ]),
        );
        const datacenter = spyOn(
            serviceData,
            "getDatacenterServerReferential",
        ).and.returnValue(
            of([
                {
                    uid: "",
                    name: "Default DC",
                    location: "France",
                    pue: 1.5,
                    displayLabel: "Default DC (France - PUE = 1.5)",
                },
            ]),
        );
        var server = component.server;
        server.type = "Storage";
        server.uid = "uid";

        //Call
        component.setHostReferential(server.type);
        component.setDatacenterReferential(server.datacenter);
        if (server.uid === "" && !component.dataInitialized) {
            component.initializeDefaultValue();
        }
        tick();

        // get value of variables
        const hostOptions = component.hostOptions;
        const datacenterOptions = component.datacenterOptions;

        //expectations
        expect(host).toHaveBeenCalled();
        expect(hostOptions[0]).toEqual({
            code: 1,
            value: "Server Storage S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "disk",
                    value: 36,
                },
            ],
        });
        expect(hostOptions[1]).toEqual({
            code: 2,
            value: "Server Storage M",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "disk",
                    value: 56,
                },
            ],
        });
        expect(hostOptions).toHaveSize(2);
        expect(datacenter).toHaveBeenCalled();
        expect(datacenterOptions).toEqual([
            {
                uid: "",
                name: "Default DC",
                location: "France",
                pue: 1.5,
                displayLabel: "Default DC (France - PUE = 1.5)",
            },
        ]);
        expect(datacenterOptions).toHaveSize(1);
    }));

    it("should initialize default value", () => {
        //spy
        const changeDefaultValueSpy = spyOn(component, "changeDefaultValue");
        var server = component.server;

        //mock
        const datacenters = component.datacenterOptions;
        datacenters.push({
            uid: "",
            name: "Default DC",
            location: "France",
            pue: 1.5,
            displayLabel: "Default DC (France - PUE = 1.5)",
        });

        //call
        component.initializeDefaultValue();

        //expectation
        fixture.detectChanges();
        server = component.server;
        expect(server.datacenter).toEqual(datacenters[0]);
        expect(server.quantity).toEqual(1);
        expect(server.annualOperatingTime).toEqual(8760);
        expect(changeDefaultValueSpy).toHaveBeenCalled();
    });

    it("should change default value when compute and all info known", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Compute";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Compute S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "annualElectricityConsumption",
                    value: 56,
                },
                {
                    code: "vCPU",
                    value: 36,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["vcpu"].setValue(
            hostOptions[0].characteristic[2].value,
        );
        component.serverForm.controls["lifespan"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toEqual(56);
        expect(component.server.annualElectricConsumption).toEqual(56);
        expect(component.serverForm.value.lifespan).toEqual(5);
        expect(component.server.lifespan).toEqual(5);
        expect(component.serverForm.value.vcpu).toEqual(36);
        expect(component.server.totalVCpu).toEqual(36);
    });

    it("should change default value when compute and missing vcpu info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Compute";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Compute S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "annualElectricityConsumption",
                    value: 56,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["vcpu"].setValue(null);
        component.serverForm.controls["lifespan"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toEqual(56);
        expect(component.server.annualElectricConsumption).toEqual(56);
        expect(component.serverForm.value.lifespan).toEqual(5);
        expect(component.server.lifespan).toEqual(5);
        expect(component.serverForm.value.vcpu).toBeNull();
        expect(component.server.totalVCpu).toBeNull();
    });

    it("should change default value when compute and missing lifespan info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Compute";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Compute S",
            characteristic: [
                {
                    code: "vCPU",
                    value: 5,
                },
                {
                    code: "annualElectricityConsumption",
                    value: 56,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["vcpu"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        component.serverForm.controls["lifespan"].setValue(null);
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toEqual(56);
        expect(component.server.annualElectricConsumption).toEqual(56);
        expect(component.serverForm.value.vcpu).toEqual(5);
        expect(component.server.totalVCpu).toEqual(5);
        expect(component.serverForm.value.lifespan).toBeNull();
        expect(component.server.lifespan).toBeNull();
    });

    it("should change default value when compute and missing annualElectricityConsumption info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Compute";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Compute S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "vCPU",
                    value: 56,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(null);
        component.serverForm.controls["vcpu"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["lifespan"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        server = component.server;
        expect(component.serverForm.value.vcpu).toEqual(56);
        expect(component.server.totalVCpu).toEqual(56);
        expect(component.serverForm.value.lifespan).toEqual(5);
        expect(component.server.lifespan).toEqual(5);
        expect(component.serverForm.value.electricityConsumption).toBeNull();
        expect(component.server.annualElectricConsumption).toBeNull();
    });

    it("should change default value when compute and missing all info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Compute";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Compute S",
            characteristic: [],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(null);
        component.serverForm.controls["vcpu"].setValue(null);
        component.serverForm.controls["lifespan"].setValue(null);
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toBeNull();
        expect(component.server.annualElectricConsumption).toBeNull();
        expect(component.serverForm.value.lifespan).toBeNull();
        expect(component.server.lifespan).toBeNull();
        expect(component.serverForm.value.vcpu).toBeNull();
        expect(component.server.totalVCpu).toBeNull();
    });

    it("should change default value when Storage and all info known", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Storage";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Storage S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "annualElectricityConsumption",
                    value: 56,
                },
                {
                    code: "disk",
                    value: 36,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["disk"].setValue(
            hostOptions[0].characteristic[2].value,
        );
        component.serverForm.controls["lifespan"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toEqual(56);
        expect(component.server.annualElectricConsumption).toEqual(56);
        expect(component.serverForm.value.lifespan).toEqual(5);
        expect(component.server.lifespan).toEqual(5);
        expect(component.serverForm.value.disk).toEqual(36);
        expect(component.server.totalDisk).toEqual(36);
    });

    it("should change default value when Storage and missing vcpu info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Storage";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Storage S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "annualElectricityConsumption",
                    value: 56,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["disk"].setValue(null);
        component.serverForm.controls["lifespan"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toEqual(56);
        expect(component.server.annualElectricConsumption).toEqual(56);
        expect(component.serverForm.value.lifespan).toEqual(5);
        expect(component.server.lifespan).toEqual(5);
        expect(component.serverForm.value.disk).toBeNull();
        expect(component.server.totalDisk).toBeNull();
    });

    it("should change default value when Storage and missing lifespan info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Storage";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Storage S",
            characteristic: [
                {
                    code: "disk",
                    value: 5,
                },
                {
                    code: "annualElectricityConsumption",
                    value: 56,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["disk"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        component.serverForm.controls["lifespan"].setValue(null);
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toEqual(56);
        expect(component.server.annualElectricConsumption).toEqual(56);
        expect(component.serverForm.value.disk).toEqual(5);
        expect(component.server.totalDisk).toEqual(5);
        expect(component.serverForm.value.lifespan).toBeNull();
        expect(component.server.lifespan).toBeNull();
    });

    it("should change default value when Storage and missing annualElectricityConsumption info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Storage";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Storage S",
            characteristic: [
                {
                    code: "lifespan",
                    value: 5,
                },
                {
                    code: "disk",
                    value: 56,
                },
            ],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(null);
        component.serverForm.controls["disk"].setValue(
            hostOptions[0].characteristic[1].value,
        );
        component.serverForm.controls["lifespan"].setValue(
            hostOptions[0].characteristic[0].value,
        );
        server = component.server;
        expect(component.serverForm.value.disk).toEqual(56);
        expect(component.server.totalDisk).toEqual(56);
        expect(component.serverForm.value.lifespan).toEqual(5);
        expect(component.server.lifespan).toEqual(5);
        expect(component.serverForm.value.electricityConsumption).toBeNull();
        expect(component.server.annualElectricConsumption).toBeNull();
    });

    it("should change default value when Storage and missing all info", () => {
        //mock data
        var server = component.server;
        server.host = {
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        };
        server.type = "Storage";
        var hostOptions = component.hostOptions;
        hostOptions.push({
            code: 1,
            value: "Server Storage S",
            characteristic: [],
        });

        //call
        component.changeDefaultValue();

        //expectation
        fixture.detectChanges();
        component.serverForm.controls["electricityConsumption"].setValue(null);
        component.serverForm.controls["disk"].setValue(null);
        component.serverForm.controls["lifespan"].setValue(null);
        server = component.server;
        expect(component.serverForm.value.electricityConsumption).toBeNull();
        expect(component.server.annualElectricConsumption).toBeNull();
        expect(component.serverForm.value.lifespan).toBeNull();
        expect(component.server.lifespan).toBeNull();
        expect(component.serverForm.value.disk).toBeNull();
        expect(component.server.totalDisk).toBeNull();
    });

    it("should add a datacenter to the datacenter options", () => {
        //Mock
        const newDc = {
            uid: "tsc2e0c-157c-4eb2-bb38-d81cer720e1c2",
            name: "Default DC",
            location: "France",
            pue: 1.5,
            displayLabel: "Default DC (France - PUE = 1.5)",
        };

        //call function
        component.addDatacenter(newDc);

        //expectation
        fixture.detectChanges();
        expect(component.datacenterOptions).toHaveSize(1);
        expect(component.server.datacenter).toEqual(newDc);
    });

    it("should navigate to step create when click on previous", () => {
        //spy
        const navigateSpy = spyOn(router, "navigate");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");

        //call
        component.previousStep();

        //expectation
        fixture.detectChanges();
        const server = component.server;
        expect(navigateSpy).toHaveBeenCalled();
        expect(setServerSpy).toHaveBeenCalledWith(server);
    });

    it("should navigate to vm if server=shared when click on next", () => {
        //spy and mock data
        const navigateSpy = spyOn(router, "navigate");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");
        var server = component.server;
        server.mutualizationType = "Shared";

        //call
        component.nextStep();

        //expectation
        fixture.detectChanges();
        expect(setServerSpy).toHaveBeenCalledWith(server);
        expect(navigateSpy).toHaveBeenCalled();
    });

    it("should create new server in service when Dedicated and click on next", fakeAsync(() => {
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
            }),
        );
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");
        const close = spyOn(component, "close");
        var server = component.server;
        server.mutualizationType = "Dedicated";
        server.uid = "uid";
        var digitalService = component.digitalService;
        digitalService.servers = [];

        //call
        component.nextStep();
        tick();

        //expectation
        expect(setServerSpy).toHaveBeenCalledWith(server);
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
            }),
        );
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");
        const close = spyOn(component, "close");
        var server = component.server;
        server.mutualizationType = "Dedicated";
        server.uid = "uid";
        var digitalService = component.digitalService;
        digitalService.servers = [server];

        //call
        component.nextStep();
        tick();

        //expectation
        expect(setServerSpy).toHaveBeenCalledWith(server);
        expect(close).toHaveBeenCalled();
        expect(update).toHaveBeenCalledWith(digitalService);
        expect(digitalService.servers).toHaveSize(1);
    }));

    it("should close panel", () => {
        const close = spyOn(serviceBusiness, "closePanel");
        serviceBusiness.panelSubject$.subscribe((boolean) => {
            expect(boolean).toBe(false);
        });

        component.close();

        expect(close).toHaveBeenCalled();
    });
});
