/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { Injectable } from "@angular/core";
import { Observable, ReplaySubject, filter, map } from "rxjs";
import { UserDataService } from "../data/user-data.service";
import {
    Organization,
    OrganizationData,
    Subscriber,
} from "./../../interfaces/user.interfaces";

import { NavigationEnd, Router } from "@angular/router";
import { Role } from "../../interfaces/roles.interfaces";
import { User } from "../../interfaces/user.interfaces";
import { MessageService } from "primeng/api";
import { TranslateService } from "@ngx-translate/core";

@Injectable({
    providedIn: "root",
})
export class UserService {
    private organizationSubject = new ReplaySubject<OrganizationData>(1);

    private subscriberSubject = new ReplaySubject<string>(1);

    private rolesSubject = new ReplaySubject<Role[]>(1);

    roles$ = this.rolesSubject.asObservable();

    currentSubscriber$ = this.subscriberSubject.asObservable();

    currentOrganization$ = this.organizationSubject.asObservable();

    organizations$: Observable<any[]> = this.userDataService.userSubject
        .asObservable()
        .pipe(
            map((user: User) =>
                user.subscribers.flatMap((subscriber: any) =>
                    subscriber.organizations.map((organization: Organization) => ({
                        name: organization.name,
                        organization: organization,
                        subscriber: subscriber,
                        color: this.generateColor(organization.name + subscriber.name),
                    }))
                )
            )
        );

    isAllowedInventoryRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryRead))
    );

    isAllowedDigitalServiceRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceRead))
    );

    isAllowedInventoryWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryWrite))
    );

    isAllowedDigitalServiceWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceWrite))
    );
    constructor(
        private router: Router,
        private userDataService: UserDataService,
        private messageService: MessageService,
        private translate: TranslateService
    ) {
        if(this.router?.events){
        this.router.events
            .pipe(filter((event) => event instanceof NavigationEnd))
            .subscribe(() => {
                this.userDataService.userSubject.subscribe((currentUser) => {
                    let subscriber: Subscriber | undefined;
                    let organization: Organization | undefined;
                    let subscriberName: string = "";
                    let organizationName: string = "";
                    if (this.router.url == "/") {
                        // If the url is unknown, we set the default subscriber and the default organization
                        subscriber = currentUser.subscribers.find(
                            (subscriber: Subscriber) => subscriber.defaultFlag
                        );
                        if (
                            subscriber === undefined &&
                            currentUser.subscribers.length > 0
                        ) {
                            subscriber = currentUser.subscribers[0];
                        }
                        if (subscriber) {
                            organization = subscriber.organizations.find(
                                (org) => org.defaultFlag
                            );

                            if (
                                organization === undefined &&
                                subscriber.organizations.length > 0
                            ) {
                                organization = subscriber.organizations[0];
                            }
                        }
                    } else {
                        const [_, subscriberName, organizationName] =
                            this.router.url.split("/");
                        subscriber = currentUser?.subscribers.find(
                            (sub: any) => sub.name == subscriberName
                        );

                        organization = subscriber?.organizations.find(
                            (org: any) => org.name === organizationName
                        );
                    }

                    if (subscriber && organization) {
                        this.checkIfAllowedElseRedirect(subscriber, organization);
                    } else if (this.router.url !== "/") {
                        this.organizationSubject.next({
                            name: organizationName,
                            color: this.generateColor(organizationName + subscriberName),
                        });
                        this.subscriberSubject.next(subscriberName);
                    } else {
                        this.messageService.add({
                            severity: "warn",
                            summary: this.translate.instant(
                                "toast-errors.subscriber-or-organization-not-found.title"
                            ),
                            detail: this.translate.instant(
                                "toast-errors.subscriber-or-organization-not-found.text"
                            ),
                            sticky: true,
                        });
                    }
                });
            });
        }
    }

    setUserSubscription(subscriber: Subscriber, organization: Organization) {
        this.subscriberSubject.next(subscriber.name);
        this.organizationSubject.next({
            name: organization.name,
            subscriber: subscriber,
            color: this.generateColor(organization.name + subscriber.name),
        });
        this.rolesSubject.next(organization.roles);
    }

    checkIfAllowedElseRedirect(
        existingSubscriber: Subscriber,
        existingOrganization: Organization
    ) {
        this.setUserSubscription(existingSubscriber, existingOrganization);
        let subscriber: string = existingSubscriber.name;
        let roles: Role[] = existingOrganization.roles;

        // We check if the roles matches with the url, if not redirect
        if (this.router.url.includes("/inventories") && roles.includes(Role.InventoryRead)) {
            // Nothings to do, everythings fine
            return;
        }
        if (
            this.router.url.includes("/digital-services") &&
            roles.includes(Role.DigitalServiceRead)
        ) {
            // Nothings to do, everythings fine
            return;
        }

        // Otherwise, we redirect to defaults home page based on role
        this.redirectToAllowedPage(subscriber, existingOrganization);
    }

    redirectToAllowedPage(subscriber: string, organization: Organization) {
        if (organization.roles.includes(Role.InventoryRead)) {
            this.router.navigateByUrl(`${subscriber}/${organization.name}/inventories`);
            return;
        }
        if (organization.roles.includes(Role.DigitalServiceRead)) {
            this.router.navigateByUrl(
                `${subscriber}/${organization.name}/digital-services`
            );
            return;
        }
        // We didn't find a suitable home page, therefore redirect to 403.
        this.router.navigateByUrl(`something-went-wrong/403`);
    }

    generateColor(str: string): string {
        let hash = 0;
        for (let i = 0; i < str.length; i++) {
            hash = str.charCodeAt(i) + ((hash << 5) - hash);
        }
        const color = (hash & 0x00ffffff).toString(16).toUpperCase();
        return "#" + "00000".substring(0, 6 - color.length) + color;
    }
}
