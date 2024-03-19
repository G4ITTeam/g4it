/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { NgModule } from "@angular/core";
import { AccordionModule } from "primeng/accordion";
import { ButtonModule } from "primeng/button";
import { CalendarModule } from "primeng/calendar";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DividerModule } from "primeng/divider";
import { DropdownModule } from "primeng/dropdown";
import { FileUploadModule } from "primeng/fileupload";
import { ProgressSpinnerModule } from "primeng/progressspinner";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SidebarModule } from "primeng/sidebar";
import { ToastModule } from "primeng/toast";
import { SharedModule } from "src/app/core/shared/shared.module";
import { BatchStatusComponent } from "./batch-status/batch-status.component";
import { FilePanelComponent } from "./file-panel/file-panel.component";
import { SelectFileComponent } from "./file-panel/select-file/select-file.component";
import { InventoriesComponent } from "./inventories.component";
import { EquipmentsCardComponent } from "./equipments-card/equipments-card.component";
import { inventoriesRouter } from "./inventories.router";
import { InventoryItemComponent } from "./inventory-item/inventory-item.component";
import { InputTextModule } from 'primeng/inputtext';

@NgModule({
    declarations: [
        InventoriesComponent,
        InventoryItemComponent,
        FilePanelComponent,
        SelectFileComponent,
        BatchStatusComponent,
        EquipmentsCardComponent,
    ],
    imports: [
        FileUploadModule,
        CalendarModule,
        DividerModule,
        ToastModule,
        SharedModule,
        ButtonModule,
        SidebarModule,
        ScrollPanelModule,
        ConfirmPopupModule,
        DropdownModule,
        AccordionModule,
        ProgressSpinnerModule,
        InputTextModule,
        inventoriesRouter,
    ],
    exports: [InventoriesComponent],
})
export class InventoriesModule {}
