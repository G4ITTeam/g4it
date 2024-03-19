/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { SharedModule } from "src/app/core/shared/shared.module";
import { ImpactButtonComponent } from "./impact-button.component";

describe("ImpactButtonComponent", () => {
    let component: ImpactButtonComponent;
    let fixture: ComponentFixture<ImpactButtonComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ImpactButtonComponent],
            imports: [ButtonModule, SharedModule, TranslateModule.forRoot()],
            providers: [TranslatePipe, TranslateService],
        }).compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(ImpactButtonComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should set the impactImage and impactUnite based on input impact", () => {
        component.impact = "particulate-matter";
        component.ngOnInit();
        expect(component.impactImage).toBe("assets/images/icons/icon-factory.svg");
    });
});
