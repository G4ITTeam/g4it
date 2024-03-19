/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { RouterModule, Routes } from "@angular/router";
import { InventoriesCritereFootprintComponent } from "./critere/inventories-critere-footprint.component";
import { InventoriesGlobalFootprintComponent } from "./global/inventories-global-footprint.component";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";

const routes: Routes = [
    {
        path: "",
        component: InventoriesFootprintComponent,
        children: [
            {
                path: "multi-criteria",
                component: InventoriesGlobalFootprintComponent,
            },
            {
                path: ":critere",
                component: InventoriesCritereFootprintComponent,
            },
            {
                path: "",
                redirectTo: "multi-criteria",
                pathMatch: "full",
            },
        ],
    },
];

export const inventoriesFootprintRouter = RouterModule.forChild(routes);
