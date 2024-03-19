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
import { lastValueFrom } from "rxjs";
import {
    DigitalServiceNetworkConfig,
    NetworkType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-networks-side-panel",
    templateUrl: "./digital-services-networks-side-panel.component.html",
    providers : [MessageService]
})
export class DigitalServicesNetworksSidePanelComponent {
    @Input() sidebarVisible: boolean = true;

    @Input() network: DigitalServiceNetworkConfig = {
        uid: undefined,
        type: {
            code: "",
            value: "",
        },
        yearlyQuantityOfGbExchanged: 0,
    };

    @Output() sidebarVisibleChange: EventEmitter<boolean> = new EventEmitter();

    @Output() updateNetworks: EventEmitter<DigitalServiceNetworkConfig> =
        new EventEmitter();
    @Output() deleteNetworks: EventEmitter<DigitalServiceNetworkConfig> =
        new EventEmitter();

    networkTypes: NetworkType[] = [];

    networksForm = this._formBuilder.group({
        type: [{ code: "", value: "" }, Validators.required],
        yearlyQuantityOfGbExchanged: ["0", Validators.required],
    });

    constructor(
        private digitalDataService: DigitalServicesDataService,
        private _formBuilder: FormBuilder,
        private spinner: NgxSpinnerService,
        public userService:UserService
    ) {}

    ngOnInit() {
        this.getNetworksRefrentials();
    }

    async getNetworksRefrentials() {
        const referentials = await lastValueFrom(
            this.digitalDataService.getNetworkReferential()
        );
        this.networkTypes = referentials;
        this.network.type = { code: "fixed-line-network-1", value: "Fixed FR" };
    }

    async deleteNetwork() {
        this.spinner.show();
        this.deleteNetworks.emit(this.network);
        this.close();
    }

    async submitFormData() {
        this.spinner.show();

        let network = this.networksForm.get("type")!.value || "";
        this.network.type.code = JSON.parse(JSON.stringify(network)).code;
        this.network.type.value = JSON.parse(JSON.stringify(network)).value;

        this.updateNetworks.emit(this.network);
        this.close();
    }

    close() {
        this.sidebarVisibleChange.emit(false);
    }
}
