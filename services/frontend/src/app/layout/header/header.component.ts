/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, OnInit } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { Subject, takeUntil } from "rxjs";
import { BusinessHours } from "src/app/core/interfaces/business-hours.interface";
import {
    Organization,
    OrganizationData,
    Subscriber,
    User,
} from "src/app/core/interfaces/user.interfaces";
import { Version } from "src/app/core/interfaces/version.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { BusinessHoursService } from "src/app/core/service/data/business-hours.service";
import { VersionDataService } from "src/app/core/service/data/version-data.service";
import { generateColor } from "src/app/core/utils/color";
import { Constants } from "src/constants";

@Component({
    selector: "app-header",
    templateUrl: "./header.component.html",
})
export class HeaderComponent implements OnInit {
    version: Version = {
        g4it: "",
        numEcoEval: "",
    };

    selectedPage: string = "";

    selectedLanguage: string = "en";

    sideHeaderVisible: boolean = false;

    ngUnsubscribe = new Subject<void>();

    organizations: OrganizationData[] = [];

    businessHoursData: BusinessHours[] = [];

    selectedOrganization: Organization = {} as Organization;
    selectedOrganizationData: OrganizationData | undefined = undefined;
    selectedPath = "";

    currentSubscriber: Subscriber = {} as Subscriber;

    days = ["monday", "tuesday", "wednesday", "thursday", "friday"];
    weekendDays = ["saturday", "sunday"];

    isAdminOnSubscriberOrOrganization = false;

    constructor(
        private router: Router,
        public userService: UserService,
        private versionDataService: VersionDataService,
        private translate: TranslateService,
        private businessHoursService: BusinessHoursService,
    ) {
        this.router.routeReuseStrategy.shouldReuseRoute = () => {
            return false;
        };
    }

    ngOnInit() {
        this.selectedLanguage = this.translate.currentLang;
        this.getPageFromUrl();
        this.router.events.pipe(takeUntil(this.ngUnsubscribe)).subscribe((event) => {
            if (event instanceof NavigationEnd) {
                this.getPageFromUrl();
            }
        });
        this.userService.user$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((user: User) => {
                this.organizations = [];
                user.subscribers.forEach((subscriber) => {
                    subscriber.organizations.forEach((organization) => {
                        this.organizations.push({
                            color: generateColor(organization.name + subscriber.name),
                            id: organization.id,
                            name: organization.name,
                            organization,
                            subscriber: subscriber,
                        });
                    });
                });
                this.isAdminOnSubscriberOrOrganization =
                    this.userService.hasAnyAdminRole(user);
            });

        this.userService.currentSubscriber$.subscribe(
            (subscriber) => (this.currentSubscriber = subscriber),
        );

        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization: Organization) => {
                this.selectedOrganization = organization;
                this.selectedOrganizationData = {
                    color: generateColor(organization.name + this.currentSubscriber.name),
                    id: organization.id,
                    name: organization.name,
                    organization,
                    subscriber: this.currentSubscriber,
                };
                this.selectedPath = `/subscribers/${this.currentSubscriber.name}/organizations/${this.selectedOrganization?.id}`;
            });

        this.versionDataService
            .getVersion()
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((version: Version) => {
                this.version = version;
            });

        this.businessHoursService
            .getBusinessHours()
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((businessHours: BusinessHours[]) => {
                this.businessHoursData = businessHours;
            });
    }

    getPageFromUrl() {
        let [_, subscribers, subscriber, organizations, organization, page] =
            this.router.url.split("/");

        if (subscribers === "administration") {
            this.selectedPage = "administration";
        } else {
            this.selectedPage = page;
        }
    }

    changeLanguage(lang: string): void {
        this.translate.use(lang);
        localStorage.setItem("lang", lang);
        document.querySelector("html")!.setAttribute("lang", lang);
        this.router.navigate([], {
            skipLocationChange: true,
            queryParamsHandling: "merge",
        });
        window.location.reload();
    }

    changePageToDigitalServices() {
        return `/subscribers/${this.currentSubscriber.name}/organizations/${this.selectedOrganization?.id}/digital-services`;
    }

    changePageToInventories() {
        return `/subscribers/${this.currentSubscriber.name}/organizations/${this.selectedOrganization?.id}/inventories`;
    }

    changePageToAdministraion() {
        let adminAccess = `/administration/users`;
        return adminAccess;
    }

    composeEmail() {
        let subject = `[${this.currentSubscriber.name}/${this.selectedOrganization?.id}] ${Constants.SUBJECT_MAIL}`;
        let email = `mailto:${Constants.RECIPIENT_MAIL}?subject=${subject}`;
        window.location.href = email;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
