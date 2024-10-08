/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { FootprintRedirectGuard } from "src/app/guard/footprint-redirect.guard";
import { InventoriesApplicationFootprintComponent } from "./inventories-application-footprint.component";

const routes: Routes = [
    {
        path: ":criteria",
        component: InventoriesApplicationFootprintComponent,
        canActivate: [FootprintRedirectGuard],
    },
];

export const inventoriesApplicationRouteur = RouterModule.forChild(routes);
