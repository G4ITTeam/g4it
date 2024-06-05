/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component } from "@angular/core";
import { TranslateService } from "@ngx-translate/core";
import { ConfirmationService, MessageService } from "primeng/api";
import {
    OrganizationUpsertRest,
    Organization,
    Subscriber,
} from "src/app/core/interfaces/administration.interfaces";
import { AdministrationService } from "src/app/core/service/business/administration.service";
import { UserService } from "src/app/core/service/business/user.service";
import { Constants } from "src/constants";
@Component({
    selector: "app-organizations",
    templateUrl: "./organizations.component.html",
    providers: [ConfirmationService, MessageService],
})
export class OrganizationsComponent {
    subscribersDetails!: Subscriber[];
    subscriber: any;
    organizations!: Organization[];
    status = Constants.ORGANIZATION_STATUSES;
    enableList = false;
    constructor(
        private confirmationService: ConfirmationService,
        public administrationService: AdministrationService,
        private translate: TranslateService,
        private userService: UserService,
    ) {}

    ngOnInit() {
        this.administrationService.getOrganizations().subscribe((res: any) => {
            this.subscribersDetails = res;
        });
        this.userService.updateOrganization = [];
    }

    getCurrentOrganizations() {
        this.administrationService.getOrganizations().subscribe((res: any) => {
            res.find((subscriber: any) => {
                if (subscriber.name === this.subscriber.name) {
                    this.organizations = subscriber.organizations;
                    this.userService.updateOrganization = [];
                    this.organizations.map((res) => {
                        if (
                            res.status === Constants.ORGANIZATION_STATUSES.TO_BE_DELETED
                        ) {
                            this.userService.updateOrganization.push(res.id);
                        }
                    });
                }
            });
        });
    }

    confirm(event: Event, organization: Organization) {
        this.confirmationService.confirm({
            target: event.target as EventTarget,
            message: this.translate.instant("administration.delete-message"),
            header: this.translate.instant("administration.delete-confirmation"),
            icon: "pi pi-info-circle",
            acceptLabel: this.translate.instant("administration.delete"),
            acceptButtonStyleClass: "p-button-danger center",
            rejectButtonStyleClass: Constants.CONSTANT_VALUE.NONE,
            acceptIcon: Constants.CONSTANT_VALUE.NONE,
            rejectIcon: Constants.CONSTANT_VALUE.NONE,
            rejectVisible: false,

            accept: () => {
                this.updateOrganization(organization.id, {
                    subscriberId: this.subscriber.id,
                    name: organization.name,
                    status: this.status.TO_BE_DELETED,
                    dataRetentionDay: null,
                });
            },
        });
    }

    confirmToActive(organization: Organization) {
        this.updateOrganization(organization.id, {
            subscriberId: this.subscriber.id,
            name: organization.name,
            status: null,
            dataRetentionDay: 0,
        });
    }

    updateOrganization(organizationId: number, body: OrganizationUpsertRest) {
        this.administrationService
            .deleteOrganization(organizationId, body)
            .subscribe((_) => {
                this.getCurrentOrganizations();
            });
    }
}
