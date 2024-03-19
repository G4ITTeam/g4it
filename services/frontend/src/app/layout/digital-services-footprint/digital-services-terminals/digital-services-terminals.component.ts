/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component, OnInit } from "@angular/core";
import { NgxSpinnerService } from "ngx-spinner";
import {
    DigitalService,
    DigitalServiceTerminalConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";

import { lastValueFrom } from "rxjs";
import { MessageService } from "primeng/api";
import { UserService } from "src/app/core/service/business/user.service";

@Component({
    selector: "app-digital-services-terminals",
    templateUrl: "./digital-services-terminals.component.html",
    providers:[MessageService]
})
export class DigitalServicesTerminalsComponent implements OnInit {
    sidebarVisible: boolean = false;
    sidebarPurpose: string = "";
    terminal: DigitalServiceTerminalConfig = {
        uid: undefined,
        type: {
            code: "laptop-3",
            value: "Laptop",
        },
        country: "France",
        numberOfUsers: 0,
        yearlyUsageTimePerUser: 0,
    };
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

    resetTerminal() {
        this.terminal = {
            uid: undefined,
            type: {
                code: "laptop-3",
                value: "Laptop",
            },
            country: "France",
            numberOfUsers: 0,
            yearlyUsageTimePerUser: 0,
        };
    }

    setTerminal(terminal: DigitalServiceTerminalConfig, index: number) {
        this.terminal.uid = terminal.uid;
        this.terminal.creationDate = terminal.creationDate;
        this.terminal.country = terminal.country;
        this.terminal.type = terminal.type;
        this.terminal.yearlyUsageTimePerUser = terminal.yearlyUsageTimePerUser;
        this.terminal.numberOfUsers = terminal.numberOfUsers;
        this.terminal.idFront = index;
    }

    async updateTerminals(terminal: DigitalServiceTerminalConfig) {
        this.spinner.show();
        // Find the index of the terminal with the matching uid
        let existingTerminalIndex = this.digitalService.terminals?.findIndex(
            (t) => t.uid === terminal.uid
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

        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService)
        );

        this.spinner.hide();
    }

    async deleteTerminals(terminal: DigitalServiceTerminalConfig) {
        this.spinner.show();
        let existingTerminalIndex = this.digitalService.terminals?.findIndex(
            (t) => t.uid === terminal.uid
        );

        if (
            existingTerminalIndex !== -1 &&
            existingTerminalIndex !== undefined &&
            this.digitalService.terminals !== undefined
        ) {
            this.digitalService.terminals.splice(existingTerminalIndex, 1);
        }
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService)
        );

        this.spinner.hide();
    }
}
