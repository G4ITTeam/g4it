/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { Subject, firstValueFrom, take, takeUntil } from "rxjs";
import { UserDataService } from "./core/service/data/user-data.service";
import { KeycloakService } from "keycloak-angular";
import { ActivatedRoute, ActivatedRouteSnapshot, Router } from "@angular/router";

@Component({
    selector: "app-root",
    templateUrl: "./app.component.html",
})
export class AppComponent {
    ngUnsubscribe = new Subject<void>();
    constructor(
        private userService: UserDataService,
        private keycloak: KeycloakService,
        private router: Router,
    ) {}

    async ngOnInit() {
        const token = await this.keycloak.getToken();
        if (!token) {
            const loginHint = localStorage.getItem("username") || "";
            await this.keycloak.login({
                redirectUri: window.location.href,
                loginHint,
            });
        }
        const user = await firstValueFrom(this.userService.fetchUserInfo());
        localStorage.setItem("username", user.email);
    }
}
