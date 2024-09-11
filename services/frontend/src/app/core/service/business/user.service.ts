/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Injectable } from "@angular/core";
import { ReplaySubject, filter, map } from "rxjs";
import { UserDataService } from "../data/user-data.service";
import { Organization, Subscriber, User } from "./../../interfaces/user.interfaces";

import { NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { BasicRoles, Role } from "../../interfaces/roles.interfaces";

@Injectable({
    providedIn: "root",
})
export class UserService {
    public organizationSubject = new ReplaySubject<Organization>(1);

    public subscriberSubject = new ReplaySubject<Subscriber>(1);

    private rolesSubject = new ReplaySubject<Role[]>(1);

    roles$ = this.rolesSubject.asObservable();

    currentSubscriber$ = this.subscriberSubject.asObservable();

    currentOrganization$ = this.organizationSubject.asObservable();

    user$ = this.userDataService.userSubject.asObservable();

    isAllowedSubscriberAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.SubscriberAdmin)),
    );

    isAllowedOrganizationAdmin$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.OrganizationAdmin)),
    );

    isAllowedDigitalServiceRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.DigitalServiceRead)),
    );

    isAllowedInventoryRead$ = this.roles$.pipe(
        map((roles) => roles.includes(Role.InventoryRead)),
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
                            organizationId,
                            page,
                        ] = this.router.url.split("/");
                        if (subscribers === "something-went-wrong") {
                            return;
                        }

                        if (currentUser.subscribers.length === 0) {
                            this.errorMessage("subscriber-or-organization-not-found");
                            this.router.navigateByUrl(`something-went-wrong/403`);
                        }

                        if (
                            page !== undefined &&
                            ["inventories", "digital-services"].includes(page)
                        ) {
                            const subscriber = currentUser?.subscribers.find(
                                (sub: any) => sub.name == subscriberName,
                            );

                            const organization = subscriber?.organizations.find(
                                (org: any) => org.id === Number(organizationId),
                            );

                            if (subscriber === undefined) {
                                this.errorMessage("insuffisant-right-subscriber");
                                this.router.navigateByUrl("/");
                                return;
                            }
                            if (organization === undefined) {
                                this.errorMessage("insuffisant-right-organization");
                                this.router.navigateByUrl("/");
                                return;
                            }

                            if (this.checkIfAllowed(subscriber!, organization!, page)) {
                                this.setSubscriberAndOrganization(
                                    subscriber!,
                                    organization!,
                                );
                                return;
                            } else {
                                this.router.navigateByUrl(`something-went-wrong/403`);
                                return;
                            }
                        }

                        // If the url is unknown, we set the default subscriber and the default organization
                        let subscriber: Subscriber | undefined;
                        let organization: Organization | undefined;

                        const subscriberNameLS =
                            localStorage.getItem("currentSubscriber") || undefined;

                        if (subscriberNameLS) {
                            const tmpSubs = currentUser.subscribers.filter(
                                (s) => s.name === subscriberNameLS,
                            );
                            if (tmpSubs.length === 0) {
                                subscriber = currentUser.subscribers[0];
                            } else {
                                subscriber = tmpSubs[0];
                            }
                        } else {
                            subscriber = currentUser.subscribers[0];
                        }

                        if (subscriber) {
                            let organizationNameLS =
                                localStorage.getItem("currentOrganization") || undefined;

                            if (organizationNameLS && Number.isNaN(organizationNameLS)) {
                                localStorage.removeItem("currentOrganization");
                                organizationNameLS = undefined;
                            }

                            if (organizationNameLS) {
                                const tmpOrgs = subscriber.organizations.filter(
                                    (o) => o.id === Number(organizationNameLS),
                                );
                                if (tmpOrgs.length > 0) {
                                    organization = tmpOrgs[0];
                                }
                            }
                            if (organization === undefined)
                                organization = subscriber.organizations[0];
                        }

                        if (subscribers === "administration") {
                            if (this.hasAnyAdminRole(currentUser)) {
                                this.setSubscriberAndOrganization(
                                    subscriber,
                                    organization!,
                                );
                                return;
                            } else this.router.navigateByUrl(`something-went-wrong/403`);
                        }

                        if (subscriber && organization) {
                            for (const type of ["inventories", "digital-services"]) {
                                if (this.checkIfAllowed(subscriber, organization, type)) {
                                    this.setSubscriberAndOrganization(
                                        subscriber,
                                        organization,
                                    );
                                    this.router.navigateByUrl(
                                        `subscribers/${subscriber.name}/organizations/${organization.id}/${type}`,
                                    );
                                    break;
                                }
                            }
                        }
                    });
                });
        }
    }

    errorMessage(key: string) {
        this.messageService.add({
            severity: "warn",
            summary: this.translate.instant(`toast-errors.${key}.title`),
            detail: this.translate.instant(`toast-errors.${key}.text`),
        });
    }

    hasAnyAdminRole(user: User) {
        return (
            this.hasAnyOrganizationAdminRole(user) || this.hasAnySubscriberAdminRole(user)
        );
    }

    hasAnySubscriberAdminRole(user: User) {
        return user.subscribers.some((subscriber) =>
            subscriber.roles.includes(Role.SubscriberAdmin),
        );
    }

    hasAnyOrganizationAdminRole(user: User) {
        return user.subscribers.some((subscriber) =>
            subscriber.organizations.some((organization) =>
                organization.roles.includes(Role.OrganizationAdmin),
            ),
        );
    }

    getRoles(subscriber: Subscriber, organization: Organization): Role[] {
        if (subscriber.roles.includes(Role.SubscriberAdmin)) {
            return [Role.SubscriberAdmin, Role.OrganizationAdmin, ...BasicRoles];
        }

        if (organization.roles.includes(Role.OrganizationAdmin)) {
            return [Role.OrganizationAdmin, ...BasicRoles];
        }

        const roles = [...organization.roles];

        if (organization.roles.includes(Role.InventoryWrite)) {
            roles.push(Role.InventoryRead);
        }

        if (organization.roles.includes(Role.DigitalServiceWrite)) {
            roles.push(Role.DigitalServiceRead);
        }

        return roles;
    }

    checkIfAllowed(
        subscriber: Subscriber,
        organization: Organization,
        uri: string,
    ): boolean {
        let roles: Role[] = this.getRoles(subscriber, organization);

        if (uri === "inventories" && roles.includes(Role.InventoryRead)) {
            return true;
        }

        if (uri === "digital-services" && roles.includes(Role.DigitalServiceRead)) {
            return true;
        }

        if (
            uri === "administration" &&
            (roles.includes(Role.SubscriberAdmin) ||
                roles.includes(Role.OrganizationAdmin))
        ) {
            return true;
        }

        return false;
    }

    setSubscriberAndOrganization(subscriber: Subscriber, organization: Organization) {
        this.subscriberSubject.next(subscriber);
        this.organizationSubject.next(organization);
        localStorage.setItem("currentSubscriber", subscriber.name);
        localStorage.setItem("currentOrganization", organization.id.toString());
        this.rolesSubject.next(this.getRoles(subscriber, organization));
    }

    checkAndRedirect(subscriber: Subscriber, organization: Organization, page: string) {
        if (this.checkIfAllowed(subscriber, organization, page)) {
            this.setSubscriberAndOrganization(subscriber, organization);
            this.router.navigateByUrl(
                `subscribers/${subscriber.name}/organizations/${organization.id}/${page}`,
            );
        } else {
            this.router.navigateByUrl(`something-went-wrong/403`);
        }
    }
}
