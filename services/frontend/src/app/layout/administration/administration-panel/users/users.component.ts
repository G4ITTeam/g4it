/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { FormBuilder, FormGroup, Validators } from "@angular/forms";
import { UserDetails } from "src/app/core/interfaces/user.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { Constants } from "src/constants";

@Component({
    selector: "app-users",
    templateUrl: "./users.component.html",
})
export class UsersComponent {
    userDetails!: UserDetails[];
    organization: any;
    organizations: any;
    enableList = false;
    clearForm: any;
    enableSearchButton: boolean = true;
    membersAndSearchVisible = false;
    subscribersDetails: any;
    organizationlist: any[] = [];
    membersList: any;
    openSearchResult: boolean = false;
    searchResult: any;
    addOrganizationEnable: boolean = false;
    userDetail!: UserDetails;
    membersListVisible: boolean = false;
    searchForm!: FormGroup;
    constructor(
        private administrationService: AdministrationService,
        private formBuilder: FormBuilder,
    ) {}

    ngOnInit() {
        this.getUsers();
        this.searchForm = this.formBuilder.group({
            searchName: ["", Validators.pattern(/^[ A-Za-z0-9_@.-]*$/)],
        });
    }

    getUsers() {
        this.administrationService.getUsers().subscribe((res) => {
            this.subscribersDetails = res;
            this.subscribersDetails.map((res: any) => {
                res.organizations.map((org: any) => {
                    if (org.status === Constants.ORGANIZATION_STATUSES.ACTIVE) {
                        this.organizationlist.push({
                            subscriberName: res.name,
                            subscriberId: res.id,
                            organizationName: org.name,
                            organizationId: org.id,
                        });
                    }
                });
            });
            this.organizationlist = [...new Set(this.organizationlist)].map(
                (org: any) => {
                    return {
                        ...org,
                        displayLabel:
                            org.organizationName + "-" + "(" + org.subscriberName + ")",
                    };
                },
            );
        });
    }

    get searchFormControls() {
        return this.searchForm.controls;
    }

    getCurrentOrganizations(event: any) {
        this.organizations = event.value.organizations;
    }

    checkForValidation() {
        this.enableSearchButton =
            this.searchForm.value.searchName.trim().length >= 3 ? false : true;
    }

    clearSearchField() {
        this.searchForm.controls["searchName"].setValue("");
        this.enableSearchButton = true;
    }

    searchList() {
        let searchData = this.searchForm.value.searchName.trim();
        this.administrationService
            .getSearchDetails(
                searchData,
                this.organization.subscriberId,
                this.organization.organizationId,
            )
            .subscribe((res) => {
                this.searchResult = res;
            });
    }

    getUsersDetails() {
        this.administrationService
            .getUserDetails(this.organization.organizationId)
            .subscribe((res) => {
                this.membersList = res;
            });
    }

    openSidepanelForAddOrg(user: UserDetails) {
        this.addOrganizationEnable = true;
        this.userDetail = user;
    }

    isAlreadyMemberOfOrg(linkedOrgIds: Number[]) {
        return linkedOrgIds.includes(this.organization.organizationId);
    }
}
