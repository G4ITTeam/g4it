/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { NgModule } from "@angular/core";
import { RouterModule, Routes } from "@angular/router";
import { MsalGuard } from "@azure/msal-angular";
import { ErrorComponent } from "./layout/common/error/error.component";

const routes: Routes = [
    {
        path: "something-went-wrong/:err",
        component: ErrorComponent,
    },
    {
        path: ":subscriber/:organization",
        loadChildren: () =>
            import("./layout/layout.module").then((modules) => modules.LayoutModule),
        canActivate: [MsalGuard],
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
