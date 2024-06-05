/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, EventEmitter, Input, OnInit, Output } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { ConfirmationService, MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services-footprint-header",
    templateUrl: "./digital-services-footprint-header.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesFootprintHeaderComponent implements OnInit {
    @Input() digitalService: DigitalService = {
        name: "...",
        uid: "",
        creationDate: Date.now(),
        lastUpdateDate: Date.now(),
        lastCalculationDate: null,
        terminals: [],
        servers: [],
        networks: [],
    };
    @Output() digitalServiceChange = new EventEmitter<DigitalService>();
    disableCalcul = true;
    sidebarVisible: boolean = false;

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private router: Router,
        private spinner: NgxSpinnerService,
        private confirmationService: ConfirmationService,
        private translate: TranslateService,
        public userService: UserService,
        private messageService: MessageService,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$.subscribe((res) => {
            this.digitalService = res;
            this.disableCalcul = !this.canLaunchCompute();
        });
    }

    onNameUpdate(digitalServiceName: string) {
        if (digitalServiceName != "") {
            this.digitalService.name = digitalServiceName;
            this.digitalServiceChange.emit(this.digitalService);
        }
    }

    confirmDelete(event: Event) {
        this.confirmationService.confirm({
            closeOnEscape: true,
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.popup.delete-question",
            )} ${this.digitalService.name} ?
            ${this.translate.instant("digital-services.popup.delete-text")}`,
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                this.spinner.show();
                await lastValueFrom(
                    this.digitalServicesData.delete(this.digitalService.uid),
                );
                this.router.navigateByUrl(this.changePageToDigitalServices());
            },
        });
    }

    async launchCalcul() {
        this.spinner.show();
        await lastValueFrom(
            this.digitalServicesData.launchCalcul(this.digitalService.uid),
        );
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.get(this.digitalService.uid),
        );
        this.spinner.hide();
    }

    canLaunchCompute(): boolean {
        let hasDigitalServiceBeenUpdated: boolean = true;
        if (this.digitalService.lastCalculationDate != null) {
            hasDigitalServiceBeenUpdated =
                this.digitalService.lastUpdateDate >
                this.digitalService.lastCalculationDate;
        }
        const hasNetworks = this.digitalService.networks.length > 0;
        const hasTerminals = this.digitalService.terminals.length > 0;
        const hasServers = this.digitalService.servers.length > 0;
        const hasData: boolean = hasNetworks || hasTerminals || hasServers || false;
        if (hasDigitalServiceBeenUpdated && hasData) {
            return true;
        }
        return false;
    }

    changePageToDigitalServices() {
        let [_, subscribers, subscriber, organizations, organization] = this.router.url.split("/");
        return `/subscribers/${subscriber}/organizations/${organization}/digital-services`;
    }

    noteSaveValue(event: any) {
        this.digitalService.note = {
            content: event,
        } as Note;

        this.digitalServicesData.update(this.digitalService).subscribe((res) => {
            this.messageService.add({
                severity: "success",
                summary: this.translate.instant("common.note.save"),
                sticky: false,
            });
        });
    }

    noteDelete() {
        this.digitalService.note = undefined;
        this.digitalServicesData.update(this.digitalService).subscribe((res) => {
            this.messageService.add({
                severity: "success",
                summary: this.translate.instant("common.note.delete"),
                sticky: false,
            });
        });
    }
}
