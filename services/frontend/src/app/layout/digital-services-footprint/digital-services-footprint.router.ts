/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { RouterModule, Routes } from "@angular/router";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";
import { DigitalServicesNetworksComponent } from "./digital-services-networks/digital-services-networks.component";
import { DigitalServicesServersComponent } from "./digital-services-servers/digital-services-servers.component";
import { SidePanelCreateServerComponent } from "./digital-services-servers/side-panel-create-server/side-panel-create-server.component";
import { SidePanelListVmComponent } from "./digital-services-servers/side-panel-list-vm/side-panel-list-vm.component";
import { SidePanelServerParametersComponent } from "./digital-services-servers/side-panel-server-parameters/side-panel-server-parameters.component";
import { DigitalServicesTerminalsComponent } from "./digital-services-terminals/digital-services-terminals.component";

const routes: Routes = [
    {
        path: "",
        component: DigitalServicesFootprintComponent,
        children: [
            {
                path: "dashboard",
                component: DigitalServicesFootprintDashboardComponent,
            },
            {
                path: "terminals",
                component: DigitalServicesTerminalsComponent,
            },
            {
                path: "networks",
                component: DigitalServicesNetworksComponent,
            },
            {
                path: "servers",
                component: DigitalServicesServersComponent,
                children: [
                    {
                        path: "",
                        redirectTo: "create",
                        pathMatch: "full",
                    },
                    {
                        path: "create",
                        component: SidePanelCreateServerComponent,
                    },
                    {
                        path: "parameters",
                        component: SidePanelServerParametersComponent,
                    },
                    {
                        path: "vm",
                        component: SidePanelListVmComponent,
                    },
                ],
            },
            {
                path: "",
                redirectTo: "dashboard",
                pathMatch: "full",
            },
        ],
    },
];

export const digitalServicesFootprintRouter = RouterModule.forChild(routes);
