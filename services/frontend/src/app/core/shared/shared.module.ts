/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { CommonModule } from "@angular/common";
import { NgModule } from "@angular/core";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { RouterModule } from "@angular/router";
import { TranslateModule } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { OverlayModule } from "primeng/overlay";
import { RadioButtonModule } from "primeng/radiobutton";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SidebarModule } from "primeng/sidebar";
import { TabViewModule } from "primeng/tabview";
import { ToastModule } from "primeng/toast";
import { TooltipModule } from "primeng/tooltip";
import { InformationCardLongComponent } from "src/app/layout/common/information-card-long/information-card-long.component";
import { InformationCardComponent } from "src/app/layout/common/information-card/information-card.component";
import { CompaniesMenuComponent } from "src/app/layout/header/companies-menu/companies-menu.component";
import { DatavizFilterComponent } from "src/app/layout/inventories-footprint/dataviz-filter/dataviz-filter.component";
import { InventoriesHeaderFootprintComponent } from "src/app/layout/inventories-footprint/header/inventories-header-footprint.component";
import { MonthYearPipe } from "../pipes/monthyear.pipe";
import { BatchStatusRendererPipe } from "../pipes/batch-status-renderer.pipe";

@NgModule({
    declarations: [
        MonthYearPipe,
        BatchStatusRendererPipe,
        InformationCardComponent,
        InventoriesHeaderFootprintComponent,
        DatavizFilterComponent,
        InformationCardLongComponent,
        CompaniesMenuComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        RouterModule,
        ReactiveFormsModule,
        TranslateModule,
        TooltipModule,
        ToastModule,
        CardModule,
        ScrollPanelModule,
        SidebarModule,
        RadioButtonModule,
        ConfirmPopupModule,
        ButtonModule,
        CheckboxModule,
        TabViewModule,
        OverlayModule,
    ],
    exports: [
        TooltipModule,
        MonthYearPipe,
        BatchStatusRendererPipe,
        CommonModule,
        FormsModule,
        ReactiveFormsModule,
        TranslateModule,
        ToastModule,
        InformationCardComponent,
        CompaniesMenuComponent,
        CardModule,
        ScrollPanelModule,
        InventoriesHeaderFootprintComponent,
        ConfirmPopupModule,
        ButtonModule,
        DatavizFilterComponent,
        CheckboxModule,
        TabViewModule,
        OverlayModule,
        SidebarModule,
        RadioButtonModule,
        InformationCardLongComponent,
    ],
})
export class SharedModule {}
