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
import { SharedModule } from "src/app/core/shared/shared.module";
import { PieChartComponent } from "./pie-chart.component";
declare var require: any

describe("PieChartComponent", () => {
    let component: PieChartComponent;
    let fixture: ComponentFixture<PieChartComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PieChartComponent],
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
        fixture = TestBed.createComponent(PieChartComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should generate valid EChartsOption", () => {
        const chartData: any[] = require("mock-server/data/digital-service-data/digital_service_indicators_footprint.json");
        component.selectedCriteria = "acidification";
        const echartsOption: EChartsOption = component.loadPieChartOption(chartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "Access From",
                type: "pie",
                radius: "70%",
                data: [
                    { name: "Terminal", value: 0.7, tier: "Terminal" },
                    { name: "Network", value: 0.1, tier: "Network" },
                    { name: "Server", value: 0.58, tier: "Server" },
                ],
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: "rgba(0, 0, 0, 0.5)",
                    },
                },
            },
        ]);
    });
});
