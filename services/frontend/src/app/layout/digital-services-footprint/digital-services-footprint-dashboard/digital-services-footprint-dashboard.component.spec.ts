/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { NgxSpinnerService } from "ngx-spinner";
import { ConfirmationService, MessageService, SharedModule } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { InplaceModule } from "primeng/inplace";
import { InputTextModule } from "primeng/inputtext";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { DigitalServiceFootprint } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesFootprintDashboardComponent } from "./digital-services-footprint-dashboard.component";
import { ImpactButtonComponent } from "./impact-button/impact-button.component";

describe("DigitalServicesFootprintDashboardComponent", () => {
    let component: DigitalServicesFootprintDashboardComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintDashboardComponent>;
    let digitalServicesDataService: DigitalServicesDataService;
    let footprint: DigitalServiceFootprint[] = [
        {
            tier: "Servers",
            impacts: [
                {
                    criteria: "particulate-matter",
                    sipValue: 0.39,
                    unitValue: 35,
                    unit: "Disease incidence",
                },
                {
                    criteria: "acidification",
                    sipValue: 0.58,
                    unitValue: 55,
                    unit: "mol H+ eq",
                },
                {
                    criteria: "ionising-radiation",
                    sipValue: 0.16,
                    unitValue: 2,
                    unit: "kBq U-235 eq",
                },
                {
                    criteria: "resource-use",
                    sipValue: 0.12,
                    unitValue: 13,
                    unit: "kg Sb eq",
                },
                {
                    criteria: "climate-change",
                    sipValue: 0.44,
                    unitValue: 44,
                    unit: "kg CO2 eq",
                },
            ],
        },
        {
            tier: "Networks",
            impacts: [
                {
                    criteria: "particulate-matter",
                    sipValue: 0.3,
                    unitValue: 35,
                    unit: "Disease incidence",
                },
                {
                    criteria: "acidification",
                    sipValue: 0.1,
                    unitValue: 20,
                    unit: "mol H+ eq",
                },
                {
                    criteria: "ionising-radiation",
                    sipValue: 0.99,
                    unitValue: 15,
                    unit: "kBq U-235 eq",
                },
                {
                    criteria: "resource-use",
                    sipValue: 0.26,
                    unitValue: 8,
                    unit: "kg Sb eq",
                },
                {
                    criteria: "climate-change",
                    sipValue: 0.22,
                    unitValue: 16,
                    unit: "kg CO2 eq",
                },
            ],
        },
        {
            tier: "Terminals",
            impacts: [
                {
                    criteria: "particulate-matter",
                    sipValue: 0.1,
                    unitValue: 25,
                    unit: "Disease incidence",
                },
                {
                    criteria: "acidification",
                    sipValue: 0.7,
                    unitValue: 40,
                    unit: "mol H+ eq",
                },
                {
                    criteria: "ionising-radiation",
                    sipValue: 0.4,
                    unitValue: 22,
                    unit: "kBq U-235 eq",
                },
                {
                    criteria: "resource-use",
                    sipValue: 0.9,
                    unitValue: 25,
                    unit: "kg Sb eq",
                },
                {
                    criteria: "climate-change",
                    sipValue: 0.2,
                    unitValue: 54,
                    unit: "kg CO2 eq",
                },
            ],
        },
    ];

    beforeEach(async () => {
        TestBed.configureTestingModule({
            declarations: [
                DigitalServicesFootprintDashboardComponent,
                ImpactButtonComponent,
            ],
            imports: [
                ButtonModule,
                CardModule,
                ScrollPanelModule,
                HttpClientTestingModule,
                InplaceModule,
                FormsModule,
                InputTextModule,
                SharedModule,
                ConfirmPopupModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                ConfirmationService,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
                DigitalServicesDataService,
                NgxSpinnerService,
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(DigitalServicesFootprintDashboardComponent);
        digitalServicesDataService = TestBed.inject(DigitalServicesDataService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should handleChartChange correctly", () => {
        component.selectedCriteria = "Global Vision";
        component.handleChartChange("Acidification");
        expect(component.selectedCriteria).toBe("Acidification");
        expect(component.chartType).toBe("pie");

        component.handleChartChange("Acidification");
        expect(component.selectedCriteria).toBe("Global Vision");
        expect(component.chartType).toBe("radial");

        component.handleChartChange("Climate Change");
        expect(component.selectedCriteria).toBe("Climate Change");
        expect(component.chartType).toBe("pie");

        component.handleChartChange("Acidification");
        expect(component.selectedCriteria).toBe("Acidification");
        expect(component.chartType).toBe("pie");
    });

    it("should compute criteria buttons data correctly", () => {
        component.initImpacts();
        component.setCriteriaButtons(footprint);
        expect(component.impacts[0].name).toBe("climate-change");
        expect(component.impacts[0].raw).toBe(114);
        expect(component.impacts[0].peopleeq).toBe(0.8600000000000001);

        expect(component.impacts[4].name).toBe("particulate-matter");
        expect(component.impacts[4].raw).toBe(95);
        expect(component.impacts[4].peopleeq).toBe(0.7899999999999999);
    });

    it("should generate the correct translation key", () => {
        const param = "Climate Change";
        const textType = "title";

        const result = component.getTranslationKey(param, textType);

        expect(result).toBe("digital-services-cards.climate-change.title");
    });
});
