/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import {
    DigitalServiceServerConfig,
    ServerDC,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-side-panel-datacenter",
    templateUrl: "./side-panel-datacenter.component.html",
    providers:[MessageService]
})
export default class SidePanelDatacenterComponent {
    @Input() addSidebarVisible: boolean = false;
    @Output() addSidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
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
    @Output() serverChange: EventEmitter<ServerDC> = new EventEmitter();

    countries: { label: string; value: string }[] = [];
    datacenterForm = this._formBuilder.group({
        name: ["--", Validators.required],
        pue: [1, Validators.required],
        country: ["France", Validators.required],
    });

    isToLow: boolean = false;

    constructor(
        private digitalServiceData: DigitalServicesDataService,
        private _formBuilder: FormBuilder,
        public userService:UserService
    ) {}

    ngOnInit() {
        this.getCountryReferential();
    }

    async getCountryReferential() {
        const countryList = await lastValueFrom(
            this.digitalServiceData.getCountryReferential()
        );
        this.countries = countryList.sort().map((item) => ({ value: item, label: item }));
    }

    verifyPue() {
        if (this.datacenterForm.value.pue! < 1) {
            this.isToLow = true;
        } else {
            this.isToLow = false;
        }
    }

    submitFormData() {
        if (!this.isToLow) {
            let newDc: ServerDC = {
                uid: "",
                name: this.datacenterForm.value.name || "",
                location: this.datacenterForm.value.country || "",
                pue: this.datacenterForm.value.pue || 1,
            };
            this.serverChange.emit(newDc);
            this.close();
        }
    }

    close() {
        this.datacenterForm = this._formBuilder.group({
            name: ["--", Validators.required],
            pue: [1, Validators.required],
            country: ["France", Validators.required],
        });
        this.addSidebarVisibleChange.emit(false);
    }
}
