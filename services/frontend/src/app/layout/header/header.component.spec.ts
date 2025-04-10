/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { KeycloakService } from "keycloak-angular";
import { MessageService } from "primeng/api";
import { SidebarModule } from "primeng/sidebar";
import { SharedModule } from "src/app/core/shared/shared.module";
import { HeaderComponent } from "./header.component";

describe("HeaderComponent", () => {
    let component: HeaderComponent;
    let fixture: ComponentFixture<HeaderComponent>;
    let router: Router;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                RouterTestingModule,
                HttpClientTestingModule,
                SidebarModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService, MessageService, KeycloakService],
            declarations: [HeaderComponent],
        }).compileComponents();
        fixture = TestBed.createComponent(HeaderComponent);
        router = TestBed.inject(Router);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it('should set selectedPage to "inventories" when URL has "/inventories"', () => {
        Object.defineProperty(router, "url", {
            get: () => "/subscribers/sub1/organizations/org1/inventories",
        });
        component.ngOnInit();
        expect(component.selectedPage()).toEqual("inventories");
    });

    it('should set selectedPage to "digital-services" when URL has "/digital-services"', () => {
        Object.defineProperty(router, "url", {
            get: () => "/subscribers/sub1/organizations/org1/digital-services",
        });
        component.ngOnInit();
        expect(component.selectedPage()).toEqual("digital-services");
    });

    it('should set selectedPage to "administration" when URL starts has "/administration"', () => {
        Object.defineProperty(router, "url", {
            get: () => "/administration/organizations",
        });
        component.ngOnInit();
        expect(component.selectedPage()).toEqual("administration");
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
