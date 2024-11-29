/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { FootprintRedirectGuard } from "src/app/guard/footprint-redirect.guard";
import { TitleResolver } from "../common/title-resolver.service";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";

const routes: Routes = [
    {
        path: ":criteria",
        component: InventoriesFootprintComponent,
        resolve: {
            title: TitleResolver,
        },
        data: {
            titleKey: "inventories.page-title",
        },
        canActivate: [FootprintRedirectGuard],
    },
];

export const inventoriesFootprintRouter = RouterModule.forChild(routes);
