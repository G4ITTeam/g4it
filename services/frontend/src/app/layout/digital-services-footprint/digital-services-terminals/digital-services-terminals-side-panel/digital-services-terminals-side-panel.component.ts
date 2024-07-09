/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, Output } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import {
    DigitalServiceTerminalConfig,
    TerminalsType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-terminals-side-panel",
    templateUrl: "./digital-services-terminals-side-panel.component.html",
    providers: [MessageService],
})
export class DigitalServicesTerminalsSidePanelComponent {
    @Input() sidebarVisible: boolean = true;
    @Input() terminal: DigitalServiceTerminalConfig = {} as DigitalServiceTerminalConfig;

    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();
    @Output() updateTerminals: EventEmitter<DigitalServiceTerminalConfig> =
        new EventEmitter();
    @Output() deleteTerminals: EventEmitter<DigitalServiceTerminalConfig> =
        new EventEmitter();

    terminalDeviceTypes: TerminalsType[] = [];
    countries: { label: string; value: string }[] = [];

    terminalsForm!: FormGroup;

    constructor(
        private digitalDataService: DigitalServicesDataService,
        private _formBuilder: FormBuilder,
        private spinner: NgxSpinnerService,
        public userService: UserService,
    ) { }

    ngOnInit() {
        this.initForm();
        this.getTerminalsReferentials();
    }

    resetTerminal() {
        this.terminal = {
            uid: undefined,
            type: {
                code: "laptop-3",
                value: "SetByReferential",
                lifespan: 0, // forced set by referential
            },
            lifespan: 0, // forced set by referential
            country: "France",
            numberOfUsers: 0,
            yearlyUsageTimePerUser: 0,
        };
        if (this.terminalDeviceTypes.length > 0) {
            const laptop = this.terminalDeviceTypes.filter(
                (o) => o.code === this.terminal.type.code,
            );
            if (laptop.length === 0) {
                console.error(
                    "The laptop-3 reference does not exist in the device type referential.",
                );
            } else {
                this.terminal.type = laptop[0];
                this.terminal.lifespan = this.terminal.type.lifespan;
            }
        }
    }

    initForm() {
        this.terminalsForm = this._formBuilder.group({
            type: [{ code: "", value: "", lifespan: null }, Validators.required],
            country: ["", Validators.required],
            numberOfUsers: ["0", Validators.required],
            lifespan: [null, Validators.required],
            yearlyUsageTimePerUser: ["0", Validators.required],
        });
    }

    async getTerminalsReferentials() {
        const referentials = await lastValueFrom(
            this.digitalDataService.getDeviceReferential(),
        );
        this.terminalDeviceTypes = referentials.sort((a, b) =>
            a.value.localeCompare(b.value),
        );
        this.terminal.type = this.terminalDeviceTypes[0];

        const countryList = await lastValueFrom(
            this.digitalDataService.getCountryReferential(),
        );
        this.countries = countryList.sort().map((item) => ({ value: item, label: item }));
        this.terminal.country = this.countries[0].value;

        this.resetTerminal();
    }

    close() {
        this.sidebarVisibleChange.emit(false);
    }

    async submitFormData() {
        this.spinner.show();
        this.updateTerminals.emit(this.terminal);
        this.close();
    }

    async deleteTerminal() {
        this.spinner.show();
        this.deleteTerminals.emit(this.terminal);
        this.close();
    }
}
