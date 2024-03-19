/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Component } from "@angular/core";
import { MsalBroadcastService, MsalService } from "@azure/msal-angular";
import { EventMessage, EventType, InteractionStatus } from "@azure/msal-browser";
import { Subject, filter, takeUntil } from "rxjs";
import { UserDataService } from "./core/service/data/user-data.service";

@Component({
    selector: "app-root",
    templateUrl: "./app.component.html",
})
export class AppComponent {
    ngUnsubscribe = new Subject<void>();

    constructor(
        private authService: MsalService,
        private msalBroadcastService: MsalBroadcastService,
        private userService: UserDataService
    ) {}

    ngOnInit(): void {
        this.msalBroadcastService.msalSubject$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((message: EventMessage) => {
                if (message.eventType == EventType.HANDLE_REDIRECT_END) {
                    this.userService
                        .fetchUserInfo()
                        .pipe(takeUntil(this.ngUnsubscribe))
                        .subscribe();
                }
            });

        this.msalBroadcastService.inProgress$
            .pipe(
                filter(
                    (status: InteractionStatus) =>
                        status === InteractionStatus.None ||
                        status === InteractionStatus.HandleRedirect
                ),
                takeUntil(this.ngUnsubscribe)
            )
            .subscribe(() => {
                this.checkAndSetActiveAccount();
            });
    }

    checkAndSetActiveAccount() {
        const activeAccount = this.authService.instance.getActiveAccount();

        if (!activeAccount && this.authService.instance.getAllAccounts().length > 0) {
            const accounts = this.authService.instance.getAllAccounts();
            this.authService.instance.setActiveAccount(accounts[0]);
        }
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
