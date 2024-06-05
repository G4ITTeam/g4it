/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Role } from 'src/app/core/interfaces/roles.interfaces';
import { UserDetails } from 'src/app/core/interfaces/user.interfaces';
import { AdministrationService } from 'src/app/core/service/business/administration.service';
import { Constants } from 'src/constants';

@Component({
    selector: "app-add-organization",
    templateUrl: "./add-organization.component.html"
})
export class AddOrganizationComponent {
    @Input() userDetail!: UserDetails;
    @Input() organization: any;
    @Input() clearForm!: false;
    @Output() close: EventEmitter<any> = new EventEmitter();
    role: any;
    dsModule: any;
    isModule: any;
    roleValues = Constants.ROLE_VALUES;
    isModuleValues = Constants.IS_MODULE_VALUES;
    DSModuleValues = Constants.DS_MODULE_VALUES;
    email!: String;
    isAdmin: boolean = false;

    constructor(public administrationService: AdministrationService,) { }
    ngOnInit() {
        this.clearFormData();
    }
    ngOnChanges() {
        this.email = this.userDetail?.email;
        if (this.clearForm) {
            this.clearFormData();
        }
    }

    addToOrg() {
        let roles = [];
        if (this.isModule?.id === 2) {
            roles.push(this.administrationService.updateISModuleValue(1),
                this.administrationService.updateISModuleValue(2))
        } else {
            if (this.isModule?.id === 1) {
                roles.push(this.administrationService.updateISModuleValue(this.isModule?.id))
            }
        }
        if (this.dsModule?.id === 2) {
            roles.push(this.administrationService.updateDSModuleValue(1),
                this.administrationService.updateDSModuleValue(2))
        } else {
            if (this.dsModule?.id === 1) {
                roles.push(this.administrationService.updateDSModuleValue(this.dsModule?.id))
            }
        }
        if (this.role?.value === "admin") {
            roles.push(Role.OrganizationAdmin)
        }
        let body = {
            organizationId: this.organization.organizationId,
            users: [
                {
                    userId: this.userDetail?.id,
                    roles: roles
                }
            ]
        }
        this.administrationService.postOrganization(body).subscribe(res => {
            this.close.emit(false);
        })
    }

    cancel() {
        this.clearFormData();
        this.close.emit(false);
    }

    clearFormData() {
        this.role = this.roleValues[1];
        this.dsModule = Constants.DS_MODULE_VALUES[0];
        this.isModule = Constants.IS_MODULE_VALUES[0];
    }

    validateOnAdmin(event: any) {
        if (event.value.value === Constants.ADMIN) {
            this.dsModule = Constants.DS_MODULE_VALUES[1];
            this.isModule = Constants.IS_MODULE_VALUES[1];
            this.isAdmin = true;
        } else {
            this.isAdmin = false;
        }
    }

}
