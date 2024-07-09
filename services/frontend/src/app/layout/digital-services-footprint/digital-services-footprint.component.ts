/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { ActivatedRoute } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { MenuItem } from "primeng/api";
import { lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-footprint",
    templateUrl: "./digital-services-footprint.component.html",
})
export class DigitalServicesFootprintComponent implements OnInit {
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
    tabItems: MenuItem[] | undefined;

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private route: ActivatedRoute,
        private spinner: NgxSpinnerService,
        private translate: TranslateService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.spinner.show();
        const uid = this.route.snapshot.paramMap.get("digitalServiceId") ?? "";
        const digitalService = await lastValueFrom(this.digitalServicesData.get(uid));
        // If the digital service is not found, 404 is catched by the interceptor.
        // Therefore we can continue without those verifications.
        this.digitalService = digitalService;
        this.spinner.hide();
        this.updateTabItems();
    }

    updateTabItems() {
        this.tabItems = [
            {
                label: this.translate.instant("digital-services.Terminal"),
                routerLink: "terminals",
            },
            {
                label: this.translate.instant("digital-services.Network"),
                routerLink: "networks",
            },
            {
                label: this.translate.instant("digital-services.Server"),
                routerLink: "servers",
            },
            {
                label: "Filler",
                separator: true,
                style: { visibility: "hidden", flex: 1 },
            },
            {
                label: this.translate.instant("digital-services.visualize"),
                routerLink: "dashboard",
                disabled: this.digitalService.lastCalculationDate === null,
            },
        ];
    }

    async updateDigitalService() {
        // digital service is already updated thanks to data binding
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService),
        );
        this.updateTabItems();
    }
}
