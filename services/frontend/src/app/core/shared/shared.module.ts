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
import { BatchStatusRendererPipe } from "../pipes/batch-status-renderer.pipe";
import { DecimalsPipe } from "../pipes/decimal.pipe";
import { MonthYearPipe } from "../pipes/monthyear.pipe";

import { ConfirmDialogModule } from "primeng/confirmdialog";
import { DropdownModule } from "primeng/dropdown";
import { EditorModule } from "primeng/editor";
import { InputTextModule } from "primeng/inputtext";
import { TableModule } from "primeng/table";
import { TabMenuModule } from "primeng/tabmenu";
import { CommonEditorComponent } from "src/app/layout/common/common-editor/common-editor.component";
import { HeaderComponent } from "src/app/layout/header/header.component";
import { BusinessHoursRendererPipe } from "../pipes/business-hours-renderer.pipe";
import { IntegerPipe } from "../pipes/integer.pipe";

@NgModule({
    declarations: [
        MonthYearPipe,
        DecimalsPipe,
        IntegerPipe,
        BatchStatusRendererPipe,
        InformationCardComponent,
        InventoriesHeaderFootprintComponent,
        DatavizFilterComponent,
        InformationCardLongComponent,
        CompaniesMenuComponent,
        CommonEditorComponent,
        HeaderComponent,
        BusinessHoursRendererPipe,
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
        EditorModule,
        DropdownModule,
        ConfirmDialogModule,
        TabMenuModule,
        InputTextModule,
    ],
    exports: [
        TooltipModule,
        MonthYearPipe,
        DecimalsPipe,
        IntegerPipe,
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
        CommonEditorComponent,
        DropdownModule,
        ConfirmDialogModule,
        TableModule,
        HeaderComponent,
        TabMenuModule,
        BusinessHoursRendererPipe,
        InputTextModule,
    ],
    providers: [DecimalsPipe, IntegerPipe],
})
export class SharedModule {}
