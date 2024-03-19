/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService } from "primeng/api";
import { first } from "rxjs";
import {
    DigitalServiceServerConfig,
    ServerVM,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-side-panel-add-vm",
    templateUrl: "./side-panel-add-vm.component.html",
    providers:[MessageService]
})
export class SidePanelAddVmComponent {
    @Input() index: number | undefined;
    @Input() addVMPanelVisible: boolean = false;
    @Output() addVMPanelVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Input() server: DigitalServiceServerConfig = {
        uid: undefined,
        name: "Server A",
        mutualizationType: "",
        type: "",
        quantity: 0,
        datacenter: {
            uid: "",
            name: "",
            location: "",
            pue: 0,
        },
        vm: [
            {
                uid: "",
                name: "",
                vCpu: 0,
                disk: 0,
                quantity: 0,
                annualOperatingTime: 0,
            },
        ],
    };

    vm: ServerVM = {
        uid: "",
        name: "",
        vCpu: 0,
        disk: 0,
        quantity: 0,
        annualOperatingTime: 0,
    };

    addVmForm = this._formBuilder.group({
        name: ["", Validators.required],
        vcpu: [0, Validators.required],
        disk: [0, Validators.required],
        quantity: [0, Validators.required],
        opratingTime: [0, Validators.required],
    });

    isValueTooHigh: boolean = false;

    constructor(
        private _formBuilder: FormBuilder,
        private digitalServiceBusiness: DigitalServiceBusinessService,
        private spinner: NgxSpinnerService,
        public userService:UserService
    ) {}

    ngAfterViewInit() {
        this.digitalServiceBusiness.serverFormSubject$.pipe(first()).subscribe((res) => {
            this.server = res;
            if (this.index === undefined) {
                const num = (this.server.vm?.length || 0) + 1;
                this.vm.name = "VM " + num;
                this.vm.vCpu = 0;
                this.vm.disk = 0;
                this.vm.quantity = 1;
                this.vm.annualOperatingTime = 8760;
            } else {
                this.vm.name = this.server.vm![this.index].name;
                this.vm.vCpu = this.server.vm![this.index].vCpu;
                this.vm.disk = this.server.vm![this.index].disk;
                this.vm.quantity = this.server.vm![this.index].quantity;
                this.vm.annualOperatingTime =
                    this.server.vm![this.index].annualOperatingTime;
            }
        });
    }

    verifyValue() {
        const sum = this.sum();
        if (this.server.type === "Compute") {
            const newVM = this.addVmForm.value.vcpu || 0;
            const value2Compare = (this.server.totalVCpu || 0) - sum;
            if (newVM > value2Compare) {
                this.isValueTooHigh = true;
            } else {
                this.isValueTooHigh = false;
            }
        } else if (this.server.type === "Storage") {
            const newVM = this.addVmForm.value.disk || 0;
            const value2Compare = (this.server.totalDisk || 0) - sum;
            if (newVM > value2Compare) {
                this.isValueTooHigh = true;
            } else {
                this.isValueTooHigh = false;
            }
        }
    }

    sum() {
        let sum: number = 0;
        if (this.server.type === "Compute") {
            this.server.vm?.forEach((vm) => {
                sum += vm.vCpu;
            });
            return sum;
        } else if (this.server.type === "Storage") {
            this.server.vm?.forEach((vm) => {
                sum += vm.disk;
            });
            return sum;
        }
        return sum;
    }

    submitFormData() {
        this.vm.name = this.addVmForm.value.name || "";
        this.vm.vCpu = this.addVmForm.value.vcpu || 0;
        this.vm.disk = this.addVmForm.value.disk || 0;
        this.vm.quantity = this.addVmForm.value.quantity || 0;
        this.vm.annualOperatingTime = this.addVmForm.value.opratingTime || 0;
        this.updateVM();
    }

    async updateVM() {
        this.spinner.show();
        // If the vm with the uid exists, update it; otherwise, add the new vm
        if (this.index !== undefined && this.server.vm) {
            this.server.vm[this.index] = this.vm;
        } else {
            this.server.vm?.push(this.vm);
        }
        this.digitalServiceBusiness.setServerForm(this.server);
        this.close();
        this.spinner.hide();
    }

    close() {
        this.addVMPanelVisibleChange.emit(false);
    }
}
