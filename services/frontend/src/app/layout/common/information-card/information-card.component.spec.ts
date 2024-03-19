/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { CardModule } from "primeng/card";
import { ScrollPanelModule } from "primeng/scrollpanel";

import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { InformationCardComponent } from "./information-card.component";

describe("InformationCardComponent", () => {
    let component: InformationCardComponent;
    let fixture: ComponentFixture<InformationCardComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [InformationCardComponent],
            imports: [
                ScrollPanelModule,
                CardModule,
                SharedModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(InformationCardComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
