/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { RouterModule, Routes } from "@angular/router";
import { AdministrationPanelComponent } from "./administration-panel/administration-panel.component";
import { OrganizationsComponent } from "./administration-panel/organizations/organizations.component";
import { UsersComponent } from "./administration-panel/users/users.component";

const routes: Routes = [
    {
        path: "",
        component: AdministrationPanelComponent,
        children: [
            {
                path: "organizations",
                component: OrganizationsComponent,
            },
            {
                path:"users",
                component:UsersComponent,
            }
        ]
    },
];

export const administrationRouter = RouterModule.forChild(routes);
