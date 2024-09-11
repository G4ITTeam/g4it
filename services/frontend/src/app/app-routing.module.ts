/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { AuthGuard } from "./guard/auth.gard";
import { ErrorComponent } from "./layout/common/error/error.component";

const routes: Routes = [
    {
        path: "something-went-wrong/:err",
        component: ErrorComponent,
    },
    {
        path: "administration",
        loadChildren: () =>
            import("./layout/administration/administration.module").then(
                (modules) => modules.AdministrationModule,
            ),
        canActivate: [AuthGuard],
    },
    {
        path: "subscribers/:subscriber/organizations/:organization",
        loadChildren: () =>
            import("./layout/layout.module").then((modules) => modules.LayoutModule),
        canActivate: [AuthGuard],
    },
    {
        path: "**",
        redirectTo: "",
    },
];

@NgModule({
    imports: [RouterModule.forRoot(routes, { onSameUrlNavigation: "reload" })],
    exports: [RouterModule],
})
export class AppRoutingModule {}
