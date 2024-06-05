/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import { DigitalServiceFootprint } from "src/app/core/interfaces/digital-service.interfaces";
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
                TranslateService,
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

    it("should generate valid EChartsOption", () => {
        const radialChartData: DigitalServiceFootprint[] = require("mock-server/data/digital-service-data/digital_service_indicators_footprint.json");

        const echartsOption: EChartsOption =
            component.loadRadialChartOption(radialChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.angleAxis).toEqual({
            type: "category",
            data: [
                "criteria.particulate-matter.title",
                "criteria.acidification.title",
                "criteria.ionising-radiation.title",
                "criteria.resource-use.title",
                "criteria.climate-change.title",
            ],
        });
        expect(echartsOption.legend).toEqual({
            show: true,
            data: ["Terminal", "Network", "Server"],
            formatter: jasmine.any(Function),
        });
    });
});
