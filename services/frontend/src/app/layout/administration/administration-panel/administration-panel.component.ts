/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { Router } from "@angular/router";
import { TranslateService } from "@ngx-translate/core";
import { MenuItem } from "primeng/api";
import { UserService } from "src/app/core/service/business/user.service";
import { UserDataService } from "src/app/core/service/data/user-data.service";

@Component({
    selector: "app-administration-panel",
    templateUrl: "./administration-panel.component.html",
})
export class AdministrationPanelComponent {
    sideHeaderVisibleForOrganization = true;
    tabItems!: MenuItem[];
    tabMenuList!: MenuItem[];

    constructor( public userService: UserService,
        private userDataService:UserDataService,
        public router: Router,
        private translate: TranslateService,
    ) {}

    ngOnInit() {
        this.tabItems = [
            {
                label: this.translate.instant("administration.manage-users"),
                routerLink: "users",
            },
            {
                label: this.translate.instant("administration.manage-organizations"),
                routerLink: "organizations",
            }
        ];

        this.tabMenuList = this.tabItems;
        if (!this.userDataService.isSubscriberAdmin) {
            this.tabMenuList = this.tabItems?.slice(0,1)
        }
    }
}
