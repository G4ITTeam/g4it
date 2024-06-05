/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { OrganizationsComponent } from './organizations.component';
import { TranslateModule, TranslatePipe, TranslateService } from '@ngx-translate/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { DropdownModule } from 'primeng/dropdown';
import { ConfirmPopupModule } from 'primeng/confirmpopup';
import { SidebarModule } from 'primeng/sidebar';
import { ScrollPanelModule } from 'primeng/scrollpanel';
import { ToastModule } from 'primeng/toast';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MessageService } from 'primeng/api';
import { UserService } from 'src/app/core/service/business/user.service';
import { AdministrationDataService } from 'src/app/core/service/data/administration-data-service';
import { Injectable } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Subscriber } from 'src/app/core/interfaces/administration.interfaces';

@Injectable()
class AdministrationServiceMock extends AdministrationDataService {
    override getOrganizations(): Observable<Subscriber> {
        return of();
    }
}
describe("OrganizationsComponent", () => {
  let component: OrganizationsComponent;
  let fixture: ComponentFixture<OrganizationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
        declarations: [OrganizationsComponent],
      imports: [  HttpClientTestingModule,
        ToastModule,
        SidebarModule,
        ScrollPanelModule,
        ConfirmPopupModule,
        DropdownModule,
        BrowserAnimationsModule,
        TranslateModule.forRoot(),
    ],
    providers: [
        TranslatePipe,
        TranslateService,
        MessageService,
        UserService,
        {
            provide: AdministrationDataService,
            useClass: AdministrationServiceMock,
        },
    ],
}).compileComponents();

    fixture = TestBed.createComponent(OrganizationsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });
});
