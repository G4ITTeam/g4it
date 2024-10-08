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
import { SharedModule } from "src/app/core/shared/shared.module";
import { RadialChartComponent } from "./radial-chart.component";

declare var require: any;

describe("RadialChartComponent", () => {
    let component: RadialChartComponent;
    let fixture: ComponentFixture<RadialChartComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [RadialChartComponent],
            imports: [
                ButtonModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                {
                    provide: TranslateService,
                    useValue: {
                        currentLang: "en",
                        translations: {
                            en: {
                                criteria: {
                                    "criteria.climate-change.title": "Climate Change",
                                    "criteria.resource-use.title": "Resource Use",
                                    "criteria.ionising-radiation.title":
                                        "Ionising Radiation",
                                    "criteria.acidification.title": "Acidification",
                                    "criteria.particulate-matter.title":
                                        "Particulate Matter",
                                },
                            },
                        },
                        instant: (key: string) => key,
                    },
                },
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(RadialChartComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
