/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { Constants } from "src/constants";
import { ApplicationCriteriaFootprintComponent } from "./criteria/application-criteria-footprint.component";
import { InventoriesApplicationFootprintComponent } from "./inventories-application-footprint.component";
import { ApplicationMulticriteriaFootprintComponent } from "./multicriteria/application-multicriteria-footprint.component";

const routes: Routes = [
    {
        path: "",
        component: InventoriesApplicationFootprintComponent,
        children: [
            {
                path: Constants.MUTLI_CRITERIA,
                component: ApplicationMulticriteriaFootprintComponent,
            },
            {
                path: ":critere",
                component: ApplicationCriteriaFootprintComponent,
            },
            {
                path: "",
                redirectTo: Constants.MUTLI_CRITERIA,
                pathMatch: "full",
            },
        ],
    },
];

export const inventoriesApplicationRouteur = RouterModule.forChild(routes);
