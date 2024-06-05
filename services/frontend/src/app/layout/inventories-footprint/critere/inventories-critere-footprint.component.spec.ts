/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { InformationCardComponent } from "../../common/information-card/information-card.component";
import { ImpactAggregateInfosComponent } from "../impact-aggregate-infos/impact-aggregate-infos.component";
import { InventoriesCritereFootprintComponent } from "./inventories-critere-footprint.component";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";

describe("InventoriesCritereFootprintComponent", () => {
    let component: InventoriesCritereFootprintComponent;
    let fixture: ComponentFixture<InventoriesCritereFootprintComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                InventoriesCritereFootprintComponent,
                InformationCardComponent,
                ImpactAggregateInfosComponent,
            ],
            imports: [
                ButtonModule,
                CardModule,
                TranslateModule.forRoot(),
                NgxEchartsModule,
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
                IntegerPipe,
                DecimalsPipe
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InventoriesCritereFootprintComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });

    it("should return correct info card title", () => {
        const selectedCriteria = "acidification";
        const expectedTitle = "inventories-footprint.critere.acidification.title";

        const title = component.infoCardTitle(selectedCriteria);

        expect(title).toEqual(expectedTitle);
    });

    it("should return an empty string for null criteria in info card title", () => {
        const selectedCriteria = null;

        const title = component.infoCardTitle(selectedCriteria);

        expect(title).toEqual("");
    });

    it("should return correct info card content", () => {
        const selectedCriteria = "acidification";
        const expectedContent = "inventories-footprint.critere.acidification.text";

        const content = component.infoCardContent(selectedCriteria);

        expect(content).toEqual(expectedContent);
    });

    it("should return an empty string for null criteria in info card content", () => {
        const selectedCriteria = null;

        const content = component.infoCardContent(selectedCriteria);

        expect(content).toEqual("");
    });
});
