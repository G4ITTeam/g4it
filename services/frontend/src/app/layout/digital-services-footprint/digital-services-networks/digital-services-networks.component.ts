/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component } from "@angular/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import {
    DigitalService,
    DigitalServiceNetworkConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-networks",
    templateUrl: "./digital-services-networks.component.html",
    providers: [MessageService]
})
export class DigitalServicesNetworksComponent {
    digitalService: DigitalService = {
        name: "...",
        uid: "",
        creationDate: Date.now(),
        lastUpdateDate: Date.now(),
        lastCalculationDate: null,
        terminals: [],
        servers: [],
        networks: [],
    };
    network: DigitalServiceNetworkConfig = {
        uid: undefined,
        type: {
            code: "fixed-line-network-1",
            value: "Fixed FR",
        },
        yearlyQuantityOfGbExchanged: 0,
    };
    sidebarVisible = false;

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private spinner: NgxSpinnerService,
        public userService:UserService
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$.subscribe((res) => {
            this.digitalService = res;
        });
    }

    resetNetwork() {
        this.network = {
            uid: undefined,
            type: {
                code: "fixed-line-network-1",
                value: "Fixed FR",
            },
            yearlyQuantityOfGbExchanged: 0,
        };
    }

    setNetworks(network: DigitalServiceNetworkConfig, index: number) {
        this.network.uid = network.uid;
        this.network.creationDate = network.creationDate;
        this.network.type = network.type;
        this.network.yearlyQuantityOfGbExchanged = network.yearlyQuantityOfGbExchanged;
        this.network.idFront = index;
    }

    async deleteNetworks(network: DigitalServiceNetworkConfig) {
        this.spinner.show();
        let existingNetworkIndex = this.digitalService.networks?.findIndex(
            (t) => t.uid === network.uid
        );
        if (
            existingNetworkIndex !== -1 &&
            existingNetworkIndex !== undefined &&
            this.digitalService.networks
        ) {
            this.digitalService.networks.splice(existingNetworkIndex, 1);
        }
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService)
        );
        this.spinner.hide();
    }

    async updateNetworks(network: DigitalServiceNetworkConfig) {
        this.spinner.show();
        let existingNetworkIndex = this.digitalService.networks?.findIndex(
            (t) => t.uid === network.uid
        );

        if (
            existingNetworkIndex !== -1 &&
            existingNetworkIndex !== undefined &&
            this.digitalService.networks &&
            network.uid !== undefined
        ) {
            this.digitalService.networks[existingNetworkIndex] = network;
        } else {
            this.digitalService.networks?.push(network);
        }

        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService)
        );
        this.spinner.hide();
    }
}
