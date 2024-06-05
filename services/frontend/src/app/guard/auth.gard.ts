import { Injectable } from "@angular/core";
import {
    ActivatedRouteSnapshot,
    RouterStateSnapshot,
    UrlTree,
    Router,
} from "@angular/router";
import { KeycloakAuthGuard, KeycloakService } from "keycloak-angular";

@Injectable({
    providedIn: "root",
})
export class AuthGuard extends KeycloakAuthGuard {
    constructor(
        override readonly router: Router,
        protected readonly keycloak: KeycloakService,
    ) {
        super(router, keycloak);
    }

    async isAccessAllowed(
        route: ActivatedRouteSnapshot,
        state: RouterStateSnapshot,
    ): Promise<boolean | UrlTree> {
        return this.authenticated;
    }
}
