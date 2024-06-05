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
    public organizationSubject = new ReplaySubject<OrganizationData>(1);

    private subscriberSubject = new ReplaySubject<string>(1);

    public subscriberAdminRole:any[] = [];

    private rolesSubject = new ReplaySubject<Role[]>(1);

    roles$ = this.rolesSubject.asObservable();

    currentSubscriber$ = this.subscriberSubject.asObservable();

    currentOrganization$ = this.organizationSubject.asObservable();

    updateOrganization:any[]=[];

    organizations$: Observable<any[]> = this.userDataService.userSubject
        .asObservable()
        .pipe(
            map((user: User) =>
                user.subscribers.flatMap((subscriber: any) =>
                    subscriber.organizations.map((organization: Organization) => ({
                        id:organization.id,
                        name: organization.name,
                        organization: organization,
                        subscriber: subscriber,
                        color: this.generateColor(organization.name + subscriber.name),
                    })),
                ),
            ),
        );

    isAllowedInventoryRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryRead)),
    );

    // isAllowedSubscriberAdmin$ = this.subscriberRole.pipe(
    //     map((roles) => roles.includes(Role.SubscriberAdmin)),
    // );

    isAllowedOrganizationAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.OrganizationAdmin)),
    );

    isAllowedDigitalServiceRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceRead)),
    );

    isAllowedInventoryWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryWrite)),
    );

    isAllowedDigitalServiceWrite$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceWrite)),
    );
    constructor(
        private router: Router,
        private userDataService: UserDataService,
        private messageService: MessageService,
        private translate: TranslateService,
    ) {
        if (this.router?.events) {
            this.router.events
                .pipe(filter((event) => event instanceof NavigationEnd))
                .subscribe(() => {

                    this.userDataService.userSubject.subscribe((currentUser) => {
                        const [
                            _,
                            subscribers,
                            subscriberName,
                            organizations,
                            organizationName,
                        ] = this.router.url.split("/");

                        if (subscribers === "something-went-wrong") {
                            return;
                        }

                        let subscriber: Subscriber | undefined;
                        let organization: Organization | undefined;

                        if (currentUser?.subscribers) {
                            this.subscriberAdminRole = [];
                            currentUser?.subscribers.map(subscriber => {
                                this.subscriberAdminRole.push(subscriber)
                            }
                        )

                        }
                        if (this.router.url == "/") {
                            // If the url is unknown, we set the default subscriber and the default organization
                            subscriber = currentUser.subscribers.find(
                                (subscriber: Subscriber) => subscriber.defaultFlag,
                            );
                            if (
                                subscriber === undefined &&
                                currentUser.subscribers.length > 0
                            ) {
                                subscriber = currentUser.subscribers[0];
                            }
                            if (subscriber) {
                                organization = subscriber.organizations.find(
                                    (org) => org.defaultFlag,
                                );

                                if (
                                    organization === undefined &&
                                    subscriber.organizations.length > 0
                                ) {
                                    organization = subscriber.organizations[0];
                                }
                            }
                        } else {
                            subscriber = currentUser?.subscribers.find(
                                (sub: any) => sub.name == subscriberName,
                            );

                            organization = subscriber?.organizations.find(
                                (org: any) => org.name === organizationName,
                            );
                        }

                        if (subscriber && organization) {
                            this.checkIfAllowedElseRedirect(subscriber, organization);
                        }
                        else {
                            if (subscribers === "administration") {
                                let subscriberData: any;
                                if (currentUser.subscribers.length > 0) {
                                    subscriberData = currentUser?.subscribers.find(
                                        (sub: any) => sub.name === localStorage.getItem("currentSubscriber"),
                                    );

                                    let organizationData:any;
                                    if (subscriberData) {
                                        organizationData = subscriberData.organizations.find(
                                            (org: any) => org.name === localStorage.getItem("currentOrganization"),
                                        );
                                        this.checkIfAllowedElseRedirect(subscriberData,organizationData);
                                    }
                                }
                                return;
                            }
                            if (currentUser.subscribers.length === 0) {
                                this.messageService.add({
                                    severity: "warn",
                                    summary: this.translate.instant(
                                        "toast-errors.subscriber-or-organization-not-found.title",
                                    ),
                                    detail: this.translate.instant(
                                        "toast-errors.subscriber-or-organization-not-found.text",
                                    ),
                                    sticky: true,
                                });
                            }
                            this.router.navigateByUrl("/");

                        }
                    });
                });
        }
    }

    setUserSubscription(subscriber: Subscriber, organization: Organization) {
        this.subscriberSubject.next(subscriber.name);
        this.organizationSubject.next({
            id:organization?.id,
            name: organization.name,
            subscriber: subscriber,
            color: this.generateColor(organization.name + subscriber.name),
        });
        localStorage.setItem('currentOrganization', organization.name);
        localStorage.setItem('currentSubscriber', subscriber.name);
        this.rolesSubject.next(organization.roles);
    }

    checkIfAllowedElseRedirect(
        existingSubscriber: Subscriber,
        existingOrganization: Organization,
    ) {
        this.setUserSubscription(existingSubscriber, existingOrganization);
        let subscriber: string = existingSubscriber.name;
        let roles: Role[] = existingOrganization.roles;


        // We check if the roles matches with the url, if not redirect
        if (
            this.router.url.includes("/inventories") &&
            (roles.includes(Role.InventoryRead) || (roles.includes(Role.InventoryWrite)))
        ) {
            // Nothings to do, everythings fine
            return;
        }
        if (
            this.router.url.includes("/digital-services") &&
            (roles.includes(Role.DigitalServiceRead) || (roles.includes(Role.DigitalServiceWrite)))
        ) {
            // Nothings to do, everythings fine
            return;
        }
        if (
            this.router.url.includes("administration") && (this.userDataService.isSubscriberAdmin || this.userDataService.isOrgAdmin)
        ) {
            // Nothings to do, everythings fine
            return;
        }
        // Otherwise, we redirect to defaults home page based on role
        this.redirectToAllowedPage(subscriber, existingOrganization);
    }

    redirectToAllowedPage(subscriber: string, organization: Organization) {
        const organizationUri = `subscribers/${subscriber}/organizations/${organization.name}`;
        const adminUri = `administration/users`

        if ((organization.roles.includes(Role.InventoryRead)) || (organization.roles.includes(Role.InventoryWrite))) {
            this.router.navigateByUrl(`${organizationUri}/inventories`);
            return;
        }
        if ((organization.roles.includes(Role.DigitalServiceRead)) || (organization.roles.includes(Role.DigitalServiceWrite))) {
            this.router.navigateByUrl(`${organizationUri}/digital-services`);
            return;
        }
        if (this.userDataService.isSubscriberAdmin) {
            this.router.navigateByUrl(adminUri);
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
