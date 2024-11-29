/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit, ViewChild } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import {
    DigitalService,
    DigitalServiceServerConfig,
    ServerVM,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SidePanelAddVmComponent } from "../side-panel-add-vm/side-panel-add-vm.component";
import { DigitalServiceBusinessService } from "./../../../../core/service/business/digital-services.service";

@Component({
    selector: "app-side-panel-list-vm",
    templateUrl: "./side-panel-list-vm.component.html",
    providers: [MessageService],
})
export class SidePanelListVmComponent implements OnInit {
    @ViewChild("vmSidePanel", { static: false })
    vmSidePanel!: SidePanelAddVmComponent;

    addVMPanelVisible: boolean = false;
    index: number | undefined;

    vm: ServerVM = {
        uid: "",
        name: "",
        vCpu: 0,
        disk: 0,
        quantity: 0,
        annualOperatingTime: 0,
    };
    server: DigitalServiceServerConfig = {
        uid: undefined,
        name: "Server A",
        mutualizationType: "",
        type: "",
        quantity: 0,
        datacenter: {
            uid: "",
            name: "",
            location: "",
            pue: 0,
        },
        vm: [
            {
                uid: "",
                name: "",
                vCpu: 0,
                disk: 0,
                quantity: 0,
                annualOperatingTime: 0,
            },
        ],
    };
    digitalService: DigitalService = {} as DigitalService;

    constructor(
        private digitalServiceBusiness: DigitalServiceBusinessService,
        private digitalDataService: DigitalServicesDataService,
        private router: Router,
        private route: ActivatedRoute,
        public userService: UserService,
    ) {}

    async ngOnInit(): Promise<void> {
        this.digitalDataService.digitalService$.subscribe((res) => {
            this.digitalService = res;
        });
        this.digitalServiceBusiness.serverFormSubject$.subscribe((res) => {
            this.server = res;
        });
    }

    resetIndex() {
        this.index = undefined;
        this.addVMPanelVisible = true;
    }

    setIndex(rowIndex: number) {
        this.index = rowIndex;
        this.addVMPanelVisible = true;
    }

    deleteVm(rowNumber: number) {
        if (rowNumber !== undefined && this.server.vm !== undefined) {
            this.server.vm.splice(rowNumber, 1);
        }
    }

    previousStep() {
        this.router.navigate(["../parameters"], { relativeTo: this.route });
    }

    async submitServer() {
        this.digitalServiceBusiness.setServerForm(this.server);
        this.digitalServiceBusiness.submitServerForm(this.server, this.digitalService);
        this.close();
    }

    close() {
        this.digitalServiceBusiness.setServerForm({
            uid: "",
            name: "",
            mutualizationType: "",
            type: "",
            quantity: 0,
            datacenter: {
                uid: "",
                name: "",
                location: "",
                pue: 0,
            },
            vm: [],
        });
        this.digitalServiceBusiness.closePanel();
    }

    openSidePanel() {
        this.digitalServiceBusiness.openPanel();
    }
}
