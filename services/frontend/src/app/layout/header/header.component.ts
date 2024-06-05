/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input, OnInit } from "@angular/core";
import { NavigationEnd, Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { Subject, takeUntil } from "rxjs";
import { BusinessHours } from "src/app/core/interfaces/business-hours.interface";
import { OrganizationData } from "src/app/core/interfaces/user.interfaces";
import { Version } from "src/app/core/interfaces/version.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { VersionDataService } from "src/app/core/service/data/version-data.service";
import { BusineeHoursService } from "src/app/core/service/data/businee-hours.service";
import { Constants } from "src/constants";
import { UserDataService } from "src/app/core/service/data/user-data.service";

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
    @Input() sideHeaderVisibleForOrganization: boolean = false;

    ngUnsubscribe = new Subject<void>();

    organizations: OrganizationData[] = [];

    businessHoursData: BusinessHours[] = [];

    selectedOrganization: OrganizationData | undefined;
    days = ["monday", "tuesday", "wednesday", "thursday", "friday"];
    weekendDays = ["saturday", "sunday"];
    currentSubscriberName!: string;

    constructor(
        private router: Router,
        public userService: UserService,
        private versionDataService: VersionDataService,
        private translate: TranslateService,
        private busineeHoursService: BusineeHoursService,
        public userDataService: UserDataService,
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
        this.userService.organizations$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organizations: OrganizationData[]) => {
                this.organizations = organizations;
            });
        this.userService.currentOrganization$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organization: OrganizationData) => {
                this.selectedOrganization = organization;
            });
        this.versionDataService
            .getVersion()
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((version: Version) => {
                this.version = version;
            });
        this.busineeHoursService
            .getBusinessHours()
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((businessHours: BusinessHours[]) => {
                this.businessHoursData = businessHours;
            });

        this.userService.currentSubscriber$.subscribe(
            (res) => (this.currentSubscriberName = res),
        );
    }

    updatedOrganization() {
        this.userService.organizations$
            .pipe(takeUntil(this.ngUnsubscribe))
            .subscribe((organizations: OrganizationData[]) => {
                this.organizations = organizations.filter(
                    (obj) => !this.userService.updateOrganization.includes(obj.id),
                );
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
        return `/subscribers/${this.currentSubscriberName}/organizations/${this.selectedOrganization?.name}/digital-services`;
    }

    changePageToInventories() {
        return `/subscribers/${this.currentSubscriberName}/organizations/${this.selectedOrganization?.name}/inventories`;
    }

    changePageToAdministraion() {
        let adminAccess = `/administration/users`;
        return adminAccess;

    }

    composeEmail() {
        let subject = `[${this.currentSubscriberName}/${this.selectedOrganization?.name}] ${Constants.SUBJECT_MAIL}`;
        let email = `mailto:${Constants.RECIPIENT_MAIL}?subject=${subject}`;
        window.location.href = email;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
