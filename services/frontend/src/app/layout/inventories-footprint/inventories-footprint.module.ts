/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { CUSTOM_ELEMENTS_SCHEMA, NO_ERRORS_SCHEMA, NgModule } from "@angular/core";
import { NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { OverlayModule } from "primeng/overlay";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { TabMenuModule } from "primeng/tabmenu";
import { TabViewModule } from "primeng/tabview";
import { ToastModule } from "primeng/toast";
import { SharedModule } from "src/app/core/shared/shared.module";
import { InventoriesCritereFootprintComponent } from "./critere/inventories-critere-footprint.component";
import { DatacenterStatsComponent } from "./datacenter-stats/datacenter-stats.component";
import { InventoriesGlobalFootprintComponent } from "./global/inventories-global-footprint.component";
import { ImpactAggregateInfosComponent } from "./impact-aggregate-infos/impact-aggregate-infos.component";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";
import { inventoriesFootprintRouter } from "./inventories-footprint.router";
import { PhysicalequipmentStatsComponent } from "./physicalequipment-stats/physicalequipment-stats.component";


@NgModule({
    declarations: [
        InventoriesFootprintComponent,
        InventoriesGlobalFootprintComponent,
        InventoriesCritereFootprintComponent,
        DatacenterStatsComponent,
        PhysicalequipmentStatsComponent,
        ImpactAggregateInfosComponent,
    ],
    imports: [
        SharedModule,
        ButtonModule,
        TabMenuModule,
        TabViewModule,
        ToastModule,
        ScrollPanelModule,
        CardModule,
        OverlayModule,
        CheckboxModule,
        ConfirmPopupModule,
        NgxEchartsModule.forRoot({
            echarts: () =>
                import(
                    /* webpackChunkName: "echarts" */
                    /* webpackMode: "lazy" */
                    "src/app/core/shared/echarts.module"
                ).then((m) => m.default),
        }),

        inventoriesFootprintRouter,
    ],
    exports: [InventoriesFootprintComponent],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA,
        NO_ERRORS_SCHEMA
      ]
})
export class InventoriesFootprintModule {}
