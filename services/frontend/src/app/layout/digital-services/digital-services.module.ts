/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { NgModule } from "@angular/core";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesComponent } from "./digital-services.component";
import { digitalServicesRouter } from "./digital-services.router";

@NgModule({
    declarations: [DigitalServicesComponent],
    imports: [
        SharedModule,
        ButtonModule,
        ScrollPanelModule,
        CardModule,
        SharedModule,
        ConfirmPopupModule,
        digitalServicesRouter,
    ],
    exports: [DigitalServicesComponent],
})
export class DigitalServicesModule {}
