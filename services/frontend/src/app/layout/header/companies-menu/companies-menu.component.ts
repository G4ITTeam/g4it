/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, Input } from "@angular/core";
import { Organization, OrganizationData } from "src/app/core/interfaces/user.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { chooseTextContrast } from "src/app/core/utils/color";

@Component({
    selector: "app-companies-menu",
    templateUrl: "./companies-menu.component.html",
})
export class CompaniesMenuComponent {
    isMenuVisible: boolean = false;
    @Input() selectedPage = "inventories";
    @Input() organizations: OrganizationData[] = [];
    @Input() selectedOrganization: OrganizationData | undefined;

    constructor(public userService: UserService) {}

    toggleMenu() {
        this.isMenuVisible = !this.isMenuVisible;
    }

    selectCompany(organization: OrganizationData) {
        this.userService.checkAndRedirect(
            organization.subscriber!,
            organization.organization as Organization,
            this.selectedPage,
        );
    }

    chooseTextContrast(bgColor: string) {
        return chooseTextContrast(bgColor);
    }
}
