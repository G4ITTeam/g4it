/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { DropdownModule } from "primeng/dropdown";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { SidebarModule } from "primeng/sidebar";
import { ToastModule } from "primeng/toast";
import { UserService } from "src/app/core/service/business/user.service";
import { AddOrganizationComponent } from "./add-organization.component";

describe("AddOrganizationComponent", () => {
    let component: AddOrganizationComponent;
    let fixture: ComponentFixture<AddOrganizationComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [AddOrganizationComponent],
            imports: [
                HttpClientTestingModule,
                ToastModule,
                SidebarModule,
                ScrollPanelModule,
                ConfirmPopupModule,
                DropdownModule,
                BrowserAnimationsModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService, MessageService, UserService],
        }).compileComponents();

        fixture = TestBed.createComponent(AddOrganizationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
