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
import { OrganizationData } from "src/app/core/interfaces/user.interfaces";
import { Version } from "src/app/core/interfaces/version.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { VersionDataService } from "src/app/core/service/data/version-data.service";
import { environment } from "src/environments/environment";

@Component({
    selector: "app-header",
    templateUrl: "./header.component.html",
})
export class HeaderComponent implements OnInit {
    version : Version = {
        g4it: "",
        numEcoEval: "",
    };

    selectedPage: string = "";

    selectedLanguage: string = "en";

    sideHeaderVisible: boolean = false;

    ngUnsubscribe = new Subject<void>();

    organizations: OrganizationData[] = [];

    selectedOrganization: OrganizationData | undefined;
    days = ["monday", "tuesday", "wednesday", "thursday", "friday"];
    weekendDays = ["saturday", "sunday"];

    constructor(
        private router: Router,
        public userService: UserService,
        private versionDataService: VersionDataService,
        private translate: TranslateService,
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
    }

    getPageFromUrl() {
        if (this.router.url.includes("inventories")) {
            this.selectedPage = "inventories";
        } else if (this.router.url.includes("digital-services")) {
            this.selectedPage = "digital-services";
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
        let subscriber = this.router.url.split("/")[1];
        let organization = this.router.url.split("/")[2];
        return `/${subscriber}/${organization}/digital-services`;
    }

    changePageToInventories() {
        let subscriber = this.router.url.split("/")[1];
        let organization = this.router.url.split("/")[2];
        return `/${subscriber}/${organization}/inventories`;
    }

    ngOnDestroy() {
        this.ngUnsubscribe.next();
        this.ngUnsubscribe.complete();
    }
}
