/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { RouterModule, Routes } from "@angular/router";
import { ApplicationCriteriaFootprintComponent } from "./criteria/application-criteria-footprint.component";
import { InventoriesApplicationFootprintComponent } from "./inventories-application-footprint.component";
import { ApplicationMulticriteriaFootprintComponent } from "./multicriteria/application-multicriteria-footprint.component";

const routes: Routes = [
    {
        path: "",
        component: InventoriesApplicationFootprintComponent,
        children: [
            {
                path: "multi-criteria",
                component: ApplicationMulticriteriaFootprintComponent,
            },
            {
                path: ":critere",
                component: ApplicationCriteriaFootprintComponent,
            },
            {
                path: "",
                redirectTo: "multi-criteria",
                pathMatch: "full",
            },
        ],
    },
];

export const inventoriesApplicationRouteur = RouterModule.forChild(routes);
