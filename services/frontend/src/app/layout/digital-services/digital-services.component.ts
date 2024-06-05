/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { NgxSpinnerService } from "ngx-spinner";
import { ConfirmationService, MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { Note } from "src/app/core/interfaces/note.interface";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

@Component({
    selector: "app-digital-services",
    templateUrl: "./digital-services.component.html",
    providers: [MessageService, ConfirmationService],
})
export class DigitalServicesComponent {
    digitalServices: DigitalService[] = [];
    selectedDigitalService: DigitalService = {} as DigitalService;
    sidebarVisible = false;
    digitalServiceId: any;
    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private router: Router,
        private spinner: NgxSpinnerService,
        private confirmationService: ConfirmationService,
        private translate: TranslateService,
        private route: ActivatedRoute,
        private messageService: MessageService,
        public userService: UserService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.spinner.show();
        await this.retrieveDigitalServices();
        this.spinner.hide();
    }

    async retrieveDigitalServices() {
        this.digitalServices = await lastValueFrom(this.digitalServicesData.list());
        this.digitalServices.sort((x, y) => x.name.localeCompare(y.name));
    }

    async createNewDigitalService() {
        this.spinner.show();
        const { uid } = await lastValueFrom(this.digitalServicesData.create());
        this.spinner.hide();
        this.goToDigitalServiceFootprint(uid);
    }

    onDigitalServiceSelection(uid: string) {
        this.goToDigitalServiceFootprint(uid);
    }

    goToDigitalServiceFootprint(uid: string) {
        this.router.navigate([`${uid}/footprint/terminals`], {
            relativeTo: this.route,
        });
    }

    confirmDelete(event: Event, digitalService: DigitalService) {
        const { name, uid } = digitalService;
        this.confirmationService.confirm({
            closeOnEscape: true,
            target: event.target as EventTarget,
            acceptLabel: this.translate.instant("common.yes"),
            rejectLabel: this.translate.instant("common.no"),
            message: `${this.translate.instant(
                "digital-services.popup.delete-question",
            )} ${name} ?
            ${this.translate.instant("digital-services.popup.delete-text")}`,
            icon: "pi pi-exclamation-triangle",
            accept: async () => {
                this.spinner.show();
                await lastValueFrom(this.digitalServicesData.delete(uid));
                await this.retrieveDigitalServices();
                this.spinner.hide();
            },
        });
    }
    noteSaveValue(event: any) {
        this.selectedDigitalService.note = {
            content: event,
        } as Note;

        // Get digital services data.
        this.digitalServicesData.get(this.selectedDigitalService.uid).subscribe((res) => {
            // update note
            res.note = {
                content: event,
            };
            this.digitalServicesData.update(res).subscribe(() => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.save"),
                    sticky: false,
                });
            });
        });
    }

    noteDelete() {
        // Get digital services data.
        this.digitalServicesData.get(this.selectedDigitalService.uid).subscribe((res) => {
            // update note
            res.note = undefined;
            this.digitalServicesData.update(res).subscribe(() => {
                this.messageService.add({
                    severity: "success",
                    summary: this.translate.instant("common.note.delete"),
                    sticky: false,
                });
            });
        });
        this.selectedDigitalService.note = undefined;
    }
}
