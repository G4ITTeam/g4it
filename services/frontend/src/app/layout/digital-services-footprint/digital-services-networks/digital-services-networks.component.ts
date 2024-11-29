/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, ViewChild } from "@angular/core";
import { MessageService } from "primeng/api";
import { firstValueFrom } from "rxjs";
import {
    DigitalService,
    DigitalServiceNetworkConfig,
    NetworkType,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks-side-panel/digital-services-networks-side-panel.component";

@Component({
    selector: "app-digital-services-networks",
    templateUrl: "./digital-services-networks.component.html",
    providers: [MessageService],
})
export class DigitalServicesNetworksComponent {
    @ViewChild("networkSidePanel", { static: false })
    networkSidePanel!: DigitalServicesNetworksSidePanelComponent;
    digitalService: DigitalService = {} as DigitalService;
    networkTypes: NetworkType[] = [];
    network: DigitalServiceNetworkConfig = {} as DigitalServiceNetworkConfig;

    sidebarVisible = false;

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        public userService: UserService,
    ) {}

    async ngOnInit() {
        this.digitalService = await firstValueFrom(
            this.digitalServicesData.digitalService$,
        );

        const referentials = await firstValueFrom(
            this.digitalServicesData.getNetworkReferential(),
        );

        this.networkTypes = [...referentials];
        this.resetNetwork();
    }

    resetNetwork() {
        if (this.networkTypes.length > 0) {
            this.network = {
                uid: undefined,
                type: this.networkTypes[0],
                yearlyQuantityOfGbExchanged: 0,
            };
        }
    }

    setNetworks(network: DigitalServiceNetworkConfig, index: number) {
        this.network = { ...network };
        this.network.idFront = index;
    }

    async actionNetwork(action: string, network: DigitalServiceNetworkConfig) {
        this.sidebarVisible = false;
        if ("cancel" === action) return;
        if (!this.digitalService.networks) return;

        let index = this.digitalService.networks.findIndex((t) => t.uid === network.uid);
        if (action === "delete") {
            if (index === -1) return;
            this.digitalService.networks.splice(index, 1);
        } else if (action === "update") {
            if (index === -1) {
                this.digitalService.networks.push(network);
            } else {
                this.digitalService.networks[index] = network;
            }
        }

        this.digitalService = await firstValueFrom(
            this.digitalServicesData.update(this.digitalService),
        );
    }
}
