/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { RouterModule, Routes } from "@angular/router";
import { InventoriesComponent } from "./inventories.component";

const routes: Routes = [
    {
        path: "",
        component: InventoriesComponent,
    },
];

export const inventoriesRouter = RouterModule.forChild(routes);
