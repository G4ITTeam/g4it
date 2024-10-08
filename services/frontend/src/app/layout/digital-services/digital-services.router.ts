/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { DigitalServicesComponent } from "./digital-services.component";
import { ShareDigitalServiceComponent } from "./share-digital-service/share-digital-service.component";

const routes: Routes = [
    {
        path: "",
        component: DigitalServicesComponent,
    },
    {
        path: ":id/share/:generatedId",
        component: ShareDigitalServiceComponent,
    },
];

export const digitalServicesRouter = RouterModule.forChild(routes);
