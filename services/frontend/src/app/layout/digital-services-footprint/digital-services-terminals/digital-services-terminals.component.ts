/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import {
    DigitalService,
    DigitalServiceTerminalConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

import { MessageService } from "primeng/api";
import { lastValueFrom } from "rxjs";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-digital-services-terminals",
    templateUrl: "./digital-services-terminals.component.html",
    providers: [MessageService],
})
export class DigitalServicesTerminalsComponent implements OnInit {
    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    terminal: DigitalServiceTerminalConfig = {} as DigitalServiceTerminalConfig;
    digitalService: DigitalService = {} as DigitalService;

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        public userService: UserService,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$.subscribe((res) => {
            this.digitalService = res;
        });
    }

    setTerminal(terminal: DigitalServiceTerminalConfig, index: number) {
        this.terminal = { ...terminal };
        this.terminal.idFront = index;
    }

    resetTerminal() {
        this.terminal = {} as DigitalServiceTerminalConfig;
    }

    async updateTerminals(terminal: DigitalServiceTerminalConfig) {
        // Find the index of the terminal with the matching uid
        let existingTerminalIndex = this.digitalService.terminals?.findIndex(
            (t) => t.uid === terminal.uid,
        );
        // If the terminal with the uid exists, update it; otherwise, add the new terminal
        if (
            existingTerminalIndex !== -1 &&
            existingTerminalIndex !== undefined &&
            this.digitalService.terminals &&
            terminal.uid !== undefined
        ) {
            this.digitalService.terminals[existingTerminalIndex] = terminal;
        } else {
            this.digitalService.terminals?.push(terminal);
        }

        await lastValueFrom(this.digitalServicesData.update(this.digitalService));
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.get(this.digitalService.uid),
        );
    }

    async deleteTerminals(terminal: DigitalServiceTerminalConfig) {
        let existingTerminalIndex = this.digitalService.terminals?.findIndex(
            (t) => t.uid === terminal.uid,
        );

        if (
            existingTerminalIndex !== -1 &&
            existingTerminalIndex !== undefined &&
            this.digitalService.terminals !== undefined
        ) {
            this.digitalService.terminals.splice(existingTerminalIndex, 1);
        }
        await lastValueFrom(this.digitalServicesData.update(this.digitalService));
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.get(this.digitalService.uid),
        );
    }
}
