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
import {
    FormBuilder,
    FormsModule,
    NG_VALUE_ACCESSOR,
    ReactiveFormsModule,
} from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService, SharedModule } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { DropdownModule } from "primeng/dropdown";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { SidePanelAddVmComponent } from "./side-panel-add-vm.component";

describe("SidePanelAddVmComponent", () => {
    let component: SidePanelAddVmComponent;
    let fixture: ComponentFixture<SidePanelAddVmComponent>;
    let serviceBusiness: DigitalServiceBusinessService;

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [SidePanelAddVmComponent],
            imports: [
                HttpClientTestingModule,
                FormsModule,
                ReactiveFormsModule,
                ButtonModule,
                DropdownModule,
                InputTextModule,
                InputNumberModule,
                ButtonModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                DigitalServiceBusinessService,
                {
                    provide: NG_VALUE_ACCESSOR,
                    useExisting: forwardRef(() => SidePanelAddVmComponent),
                    multi: true,
                },
                FormBuilder,
            ],
        });
        serviceBusiness = TestBed.inject(DigitalServiceBusinessService);
        fixture = TestBed.createComponent(SidePanelAddVmComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    afterAll(() => {
        TestBed.resetTestingModule();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("isValueTooHigh should be true when Compute", () => {
        //spy
        const sum = spyOn(component, "sum").and.returnValue(0);

        //mock
        var server = component.server;
        server.type = "Compute";
        server.totalVCpu = 50;
        component.addVmForm.controls.vcpu.setValue(100);
        //call and expectation
        component.verifyValue();

        fixture.detectChanges();
        expect(component.isValueTooHigh).toBeFalse();
        expect(component.server.type).toEqual("Compute");
        expect(sum).toHaveBeenCalled();
    });

    it("isValueTooHigh should be true when Storage", () => {
        //spy
        const sum = spyOn(component, "sum").and.returnValue(0);

        //mock
        var server = component.server;
        server.type = "Storage";
        server.totalDisk = 50;
        component.addVmForm.controls.disk.setValue(51);

        //call and expectation
        component.verifyValue();

        fixture.detectChanges();
        expect(component.isValueTooHigh).toBeFalse();
        expect(component.server.type).toEqual("Storage");
        expect(sum).toHaveBeenCalled();
    });

    it("isValueTooHigh should be false when Compute", () => {
        //spy
        const sum = spyOn(component, "sum").and.returnValue(0);

        //mock
        var server = component.server;
        server.type = "Compute";
        server.totalVCpu = 50;
        component.addVmForm.controls.vcpu.setValue(49);

        //call and expectation
        component.verifyValue();

        fixture.detectChanges();
        expect(component.isValueTooHigh).toBeFalse();
        expect(component.server.type).toEqual("Compute");
        expect(sum).toHaveBeenCalled();
    });

    it("isValueTooHigh should be false when Storage", () => {
        //spy
        const sum = spyOn(component, "sum").and.returnValue(0);

        //mock
        var server = component.server;
        server.type = "Storage";
        server.totalDisk = 50;
        component.addVmForm.controls.disk.setValue(15);

        //call and expectation
        component.verifyValue();

        fixture.detectChanges();
        expect(component.isValueTooHigh).toBeFalse();
        expect(component.server.type).toEqual("Storage");
        expect(sum).toHaveBeenCalled();
    });

    it("should do the sum of all server's vcpu", () => {
        //mock
        var server = component.server;
        server.type = "Compute";
        server.vm = [
            {
                uid: "uid1",
                name: "name 1",
                vCpu: 14,
                disk: 0,
                quantity: 11,
                annualOperatingTime: 12000,
            },
            {
                uid: "uid2",
                name: "name 2",
                vCpu: 5,
                disk: 0,
                quantity: 1,
                annualOperatingTime: 8760,
            },
        ];

        //call and expectation
        const res = component.sum();
        fixture.detectChanges();
        expect(res).toEqual(159);
    });

    it("should do the sum of all server's disk", () => {
        //mock
        var server = component.server;
        server.type = "Storage";
        server.vm = [
            {
                uid: "uid1",
                name: "name 1",
                vCpu: 0,
                disk: 4,
                quantity: 11,
                annualOperatingTime: 12000,
            },
            {
                uid: "uid2",
                name: "name 2",
                vCpu: 0,
                disk: 4,
                quantity: 1,
                annualOperatingTime: 8760,
            },
        ];

        //call and expectation
        const res = component.sum();
        fixture.detectChanges();
        expect(res).toEqual(48);
    });

    it("should submit form data", () => {
        //spy
        const updateVM = spyOn(component, "updateVM");
        //mock
        var vm = component.vm;

        //form
        component.addVmForm.controls["name"].setValue("name");
        component.addVmForm.controls["vcpu"].setValue(50);
        component.addVmForm.controls["quantity"].setValue(5);
        component.addVmForm.controls["opratingTime"].setValue(15);

        //call and expectation
        component.submitFormData();
        fixture.detectChanges();
        expect(updateVM).toHaveBeenCalled();
        expect(vm.name).toEqual(component.addVmForm.value.name!);
        expect(vm.vCpu).toEqual(component.addVmForm.value.vcpu!);
        expect(vm.quantity).toEqual(component.addVmForm.value.quantity!);
        expect(vm.annualOperatingTime).toEqual(component.addVmForm.value.opratingTime!);
    });

    it("should create new vm in server", () => {
        //spy and mock data
        const close = spyOn(component, "close");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");

        var server = component.server;
        server.vm = [];
        var vm = component.vm;
        var index = component.index;
        index = undefined;

        //call
        component.updateVM();

        //expectation
        fixture.detectChanges();
        expect(close).toHaveBeenCalled();
        expect(server.vm).toHaveSize(1);
        expect(setServerSpy).toHaveBeenCalledWith(server);
        expect(server.vm).toEqual([vm]);
    });

    it("should update vm in server", () => {
        //spy and mock data
        const close = spyOn(component, "close");
        const setServerSpy = spyOn(serviceBusiness, "setServerForm");

        var server = component.server;
        server!.vm![0] = {
            uid: "uid",
            name: "name",
            vCpu: 1,
            disk: 4,
            quantity: 1,
            annualOperatingTime: 1,
        };
        var vm = component.vm;
        vm.uid = "uid2";
        vm.name = "name 2";
        vm.vCpu = 2;
        vm.disk = 2;
        vm.quantity = 2;
        vm.annualOperatingTime = 2;
        component.index = 0;
        component.vm = vm;
        component.server = server;

        //call
        component.updateVM();

        //expectation
        fixture.detectChanges();
        expect(close).toHaveBeenCalled();
        expect(server.vm).toHaveSize(1);
        expect(setServerSpy).toHaveBeenCalledWith(server);
        expect(server.vm).toEqual([vm]);
    });

    it("should close panel", () => {
        const close = spyOn(component.addVMPanelVisibleChange, "emit");

        component.close();

        expect(close).toHaveBeenCalledWith(false);
    });
});
