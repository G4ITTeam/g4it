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
import { SidebarModule } from "primeng/sidebar";
import { SharedModule } from "src/app/core/shared/shared.module";
import { CompaniesMenuComponent } from "./companies-menu.component";
import { MessageService } from "primeng/api";

describe("CompaniesMenuComponent", () => {
    let component: CompaniesMenuComponent;
    let fixture: ComponentFixture<CompaniesMenuComponent>;
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
            providers: [TranslatePipe, TranslateService, MessageService],
            declarations: [CompaniesMenuComponent],
        }).compileComponents();
        fixture = TestBed.createComponent(CompaniesMenuComponent);
        router = TestBed.inject(Router);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should choose white text for a dark color", () => {
        const darkColor = "#222222";
        const result = component.chooseTextContrast(darkColor);
        expect(result).toBe("#FFFFFF");
    });

    it("should choose black text for a light color", () => {
        const lightColor = "#DDDDDD";
        const result = component.chooseTextContrast(lightColor);
        expect(result).toBe("#000000");
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
