/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { RouterModule } from "@angular/router";
import { FormsModule } from "@angular/forms";
import { DropdownModule } from "primeng/dropdown";
import { InplaceModule } from "primeng/inplace";
import { InputNumberModule } from "primeng/inputnumber";
import { InputTextModule } from "primeng/inputtext";
import { RadioButtonModule } from "primeng/radiobutton";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { TabMenuModule } from "primeng/tabmenu";

import { NgxEchartsModule } from "ngx-echarts";
import { CardModule } from "primeng/card";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DividerModule } from "primeng/divider";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SharedModule } from "src/app/core/shared/shared.module";
import { BarChartComponent } from "./digital-services-footprint-dashboard/bar-chart/bar-chart.component";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard/digital-services-footprint-dashboard.component";
import { ImpactButtonComponent } from "./digital-services-footprint-dashboard/impact-button/impact-button.component";
import { PieChartComponent } from "./digital-services-footprint-dashboard/pie-chart/pie-chart.component";
import { RadialChartComponent } from "./digital-services-footprint-dashboard/radial-chart/radial-chart.component";
import { DigitalServicesFootprintHeaderComponent } from "./digital-services-footprint-header/digital-services-footprint-header.component";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";
import { digitalServicesFootprintRouter } from "./digital-services-footprint.router";
import { DigitalServicesNetworksSidePanelComponent } from "./digital-services-networks/digital-services-networks-side-panel/digital-services-networks-side-panel.component";
import { DigitalServicesNetworksComponent } from "./digital-services-networks/digital-services-networks.component";
import { DigitalServicesServersComponent } from "./digital-services-servers/digital-services-servers.component";
import SidePanelDatacenterComponent from "./digital-services-servers/side-panel-add-datacenter/side-panel-datacenter.component";
import { SidePanelAddVmComponent } from "./digital-services-servers/side-panel-add-vm/side-panel-add-vm.component";
import { SidePanelCreateServerComponent } from "./digital-services-servers/side-panel-create-server/side-panel-create-server.component";
import { SidePanelListVmComponent } from "./digital-services-servers/side-panel-list-vm/side-panel-list-vm.component";
import { SidePanelServerParametersComponent } from "./digital-services-servers/side-panel-server-parameters/side-panel-server-parameters.component";
import { DigitalServicesTerminalsSidePanelComponent } from "./digital-services-terminals/digital-services-terminals-side-panel/digital-services-terminals-side-panel.component";
import { DigitalServicesTerminalsComponent } from "./digital-services-terminals/digital-services-terminals.component";

@NgModule({
    declarations: [
        DigitalServicesFootprintComponent,
        DigitalServicesFootprintDashboardComponent,
        DigitalServicesFootprintHeaderComponent,
        DigitalServicesTerminalsComponent,
        DigitalServicesNetworksComponent,
        DigitalServicesServersComponent,
        ImpactButtonComponent,
        RadialChartComponent,
        PieChartComponent,
        BarChartComponent,
        DigitalServicesTerminalsSidePanelComponent,
        DigitalServicesNetworksSidePanelComponent,
        SidePanelCreateServerComponent,
        SidePanelServerParametersComponent,
        SidePanelDatacenterComponent,
        SidePanelListVmComponent,
        SidePanelAddVmComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        InplaceModule,
        RadioButtonModule,
        DropdownModule,
        InputNumberModule,
        InputTextModule,
        SharedModule,
        TabMenuModule,
        TableModule,
        SharedModule,
        CardModule,
        ScrollPanelModule,
        SidebarModule,
        ConfirmPopupModule,
        NgxEchartsModule.forRoot({
            echarts: () => import("echarts"),
        }),
        TableModule,
        RadioButtonModule,
        DividerModule,
        digitalServicesFootprintRouter,
    ],
    exports: [DigitalServicesFootprintComponent],
})
export class DigitalServicesFootprintModule {}
