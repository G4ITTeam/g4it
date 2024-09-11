/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes, UrlSegment } from "@angular/router";
import { Constants } from "src/constants";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";

const possibleValues = [Constants.MUTLI_CRITERIA, ...Constants.CRITERIAS];

const routes: Routes = [
    {
        component: InventoriesFootprintComponent,
        matcher: (url: UrlSegment[]) => {
            if (url.length === 0) return null;

            if (possibleValues.includes(url[0].path)) {
                return {
                    consumed: url,
                    posParams: {
                        criteria: new UrlSegment(url[0].path, {}),
                    },
                };
            }
            return null;
        },
    },
];

export const inventoriesFootprintRouter = RouterModule.forChild(routes);
