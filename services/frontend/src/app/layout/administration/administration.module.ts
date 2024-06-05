import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdministrationPanelComponent } from './administration-panel/administration-panel.component';
import { administrationRouter } from './administration.router';
import { SharedModule } from 'src/app/core/shared/shared.module';
import { OrganizationsComponent } from './administration-panel/organizations/organizations.component';
import { NgxSpinnerModule } from 'ngx-spinner';
import { UsersComponent } from './administration-panel/users/users.component';
import { AddOrganizationComponent } from './administration-panel/users/add-organization/add-organization.component';



@NgModule({
  declarations: [AdministrationPanelComponent, OrganizationsComponent, AddOrganizationComponent, UsersComponent],
  imports: [
    CommonModule,
    SharedModule, NgxSpinnerModule,
    administrationRouter
  ]
})

export class AdministrationModule { }
