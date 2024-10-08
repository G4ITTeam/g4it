/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { Subject, firstValueFrom } from "rxjs";
import { UserDataService } from "./core/service/data/user-data.service";
import { GlobalStoreService } from "./core/store/global.store";

@Component({
    selector: "app-root",
    templateUrl: "./app.component.html",
})
export class AppComponent {
    ngUnsubscribe = new Subject<void>();
    selectedLang: string = this.translate.currentLang;
    constructor(
        private userService: UserDataService,
        private keycloak: KeycloakService,
        private translate: TranslateService,
        private globalStoreService: GlobalStoreService,
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

        this.globalStoreService.setcriteriaList(
            this.translate.translations[this.selectedLang].criteria,
        );
    }
}
