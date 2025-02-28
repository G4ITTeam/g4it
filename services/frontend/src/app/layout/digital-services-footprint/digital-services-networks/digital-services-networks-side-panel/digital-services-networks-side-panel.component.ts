/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, inject, Input, Output } from "@angular/core";
import { FormBuilder, Validators } from "@angular/forms";
import { MessageService } from "primeng/api";
import { DigitalServiceNetworkConfig } from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServiceStoreService } from "src/app/core/store/digital-service.store";

@Component({
    selector: "app-digital-services-networks-side-panel",
    templateUrl: "./digital-services-networks-side-panel.component.html",
    providers: [MessageService],
})
export class DigitalServicesNetworksSidePanelComponent {
    protected digitalServiceStore = inject(DigitalServiceStoreService);

    @Input() network: DigitalServiceNetworkConfig = {} as DigitalServiceNetworkConfig;

    @Output() update: EventEmitter<DigitalServiceNetworkConfig> = new EventEmitter();
    @Output() delete: EventEmitter<DigitalServiceNetworkConfig> = new EventEmitter();
    @Output() cancel: EventEmitter<DigitalServiceNetworkConfig> = new EventEmitter();
    @Output() sidebarVisible: EventEmitter<boolean> = new EventEmitter();

    networksForm = this._formBuilder.group({
        type: [
            { code: "", value: "", country: "", type: "", annualQuantityOfGo: 0 },
            Validators.required,
        ],
        yearlyQuantityOfGbExchanged: [0, [Validators.required]],
    });

    constructor(
        private _formBuilder: FormBuilder,
        public userService: UserService,
    ) {}

    deleteNetwork() {
        this.delete.emit(this.network);
    }

    submitFormData() {
        this.network.type = { ...this.networksForm.get("type")!.value! };
        this.update.emit(this.network);
    }

    cancelNetwork() {
        this.cancel.emit(this.network);
    }
    close() {
        this.cancel.emit(this.network);
        this.sidebarVisible.emit(false);
    }
}
