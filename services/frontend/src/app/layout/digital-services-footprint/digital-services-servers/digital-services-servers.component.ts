/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, ViewChild } from "@angular/core";
import { ActivatedRoute, Router } from "@angular/router";
import { MessageService } from "primeng/api";
import { Subject, lastValueFrom, takeUntil } from "rxjs";
import {
    DigitalService,
    DigitalServiceServerConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SidePanelCreateServerComponent } from "./side-panel-create-server/side-panel-create-server.component";
import { SidePanelServerParametersComponent } from "./side-panel-server-parameters/side-panel-server-parameters.component";

@Component({
    selector: "app-digital-services-servers",
    templateUrl: "./digital-services-servers.component.html",
    providers: [MessageService],
})
export class DigitalServicesServersComponent {
    @ViewChild(SidePanelServerParametersComponent)
    parameterPanel: SidePanelServerParametersComponent | undefined;
    @ViewChild(SidePanelCreateServerComponent)
    createPanel: SidePanelCreateServerComponent | undefined;
    ngUnsubscribe = new Subject<void>();

    digitalService: DigitalService = {} as DigitalService;
    sidebarVisible: boolean = false;
    existingNames: string[] = [];

    constructor(
        private digitalServicesData: DigitalServicesDataService,
        private digitalServicesBusiness: DigitalServiceBusinessService,
        private router: Router,
        private route: ActivatedRoute,
        public userService: UserService,
    ) {}

    ngOnInit() {
        this.digitalServicesData.digitalService$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.digitalService = res;
                this.existingNames = this.digitalService.servers.map(
                    (server) => server.name,
                );
                this.digitalService.servers.forEach((response) => {
                    if (response.vm) {
                        const quantity = response.vm?.map((resp) => resp.quantity);
                        if (quantity.length > 0) {
                            const sumOfVM = quantity?.reduce(
                                (vm, value) => vm + value,
                                0,
                            );
                            response.sumOfVmQuantity = sumOfVM;
                        }
                        if (response.sumOfVmQuantity === undefined) {
                            response.sumOfVmQuantity = 0;
                        }
                    }
                });
            });
        this.digitalServicesBusiness.panelSubject$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((res) => {
                this.sidebarVisible = res;
            });
    }

    ngOnDestroy() {
        if (!this.router.url.includes("servers")) {
            this.digitalServicesBusiness.closePanel();
        }
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }

    addNewServer() {
        let newServer: DigitalServiceServerConfig = {
            uid: "",
            name: this.digitalServicesBusiness.getNextAvailableName(
                this.existingNames,
                "Server",
            ),
            mutualizationType: "Dedicated",
            type: "Compute",
            quantity: 0,
            datacenter: {
                uid: "",
                name: "",
                location: "",
                pue: 0,
            },
            vm: [],
        };
        this.digitalServicesBusiness.setDataInitialized(false);
        this.digitalServicesBusiness.setServerForm(newServer);
        this.router.navigate(["create"], { relativeTo: this.route });
        this.digitalServicesBusiness.openPanel();
    }

    updateServer(server: DigitalServiceServerConfig) {
        this.digitalServicesBusiness.setDataInitialized(false);
        this.digitalServicesBusiness.setServerForm({ ...server });
        this.router.navigate(["parameters"], { relativeTo: this.route });
        this.digitalServicesBusiness.openPanel();
    }

    async deleteServers(server: DigitalServiceServerConfig) {
        let existingServerIndex = this.digitalService.servers?.findIndex(
            (t) => t.uid === server.uid,
        );
        if (
            existingServerIndex !== -1 &&
            existingServerIndex !== undefined &&
            this.digitalService.servers
        ) {
            this.digitalService.servers.splice(existingServerIndex, 1);
        }
        this.digitalService = await lastValueFrom(
            this.digitalServicesData.update(this.digitalService),
        );
    }
    closeSidebar() {
        this.digitalServicesBusiness.closePanel();
    }
}
