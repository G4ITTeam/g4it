/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import {
    DigitalServiceNetworksImpact,
    DigitalServiceServersImpact,
    DigitalServiceTerminalsImpact,
} from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServiceBusinessService } from "src/app/core/service/business/digital-services.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import * as LifeCycleUtils from "src/app/core/utils/lifecycle";
import { BarChartComponent } from "./bar-chart.component";
declare var require: any;

describe("BarChartComponent", () => {
    let component: BarChartComponent;
    let fixture: ComponentFixture<BarChartComponent>;
    let digitalServicesService: DigitalServiceBusinessService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [BarChartComponent],
            imports: [
                HttpClientTestingModule,
                ButtonModule,
                SharedModule,
                NgxEchartsModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                DigitalServiceBusinessService,
                {
                    provide: NGX_ECHARTS_CONFIG,
                    useFactory: () => ({ echarts: () => import("echarts") }),
                },
            ],
        }).compileComponents();
    });

    beforeEach(async () => {
        fixture = TestBed.createComponent(BarChartComponent);
        digitalServicesService = TestBed.inject(DigitalServiceBusinessService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should generate valid EChartsOption for Networks", () => {
        const barChartData: DigitalServiceNetworksImpact[] = require("mock-server/data/digital-service-data/digital_service_networks_footprint.json");
        component.selectedCriteria = "acidification";
        const echartsOption: EChartsOption =
            component.loadStackBarOptionNetwork(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "networks",
                type: "bar",
                data: [
                    {
                        value: 0.06719530373811722,
                        rawValue: 8.399413108825684,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.461788845062256,
                        rawValue: 0.10297897458076477,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
        expect(echartsOption.xAxis).toEqual([
            { type: "category", data: ["Fixed FR", "Mobile EU"] },
        ]);
    });

    it("should generate valid EChartsOption for Terminals (country case)", () => {
        const barChartData: DigitalServiceTerminalsImpact[] =
            digitalServicesService.transformTerminalData(
                require("mock-server/data/digital-service-data/digital_service_terminals_footprint.json"),
            );
        component.selectedCriteria = "acidification";
        component.terminalsRadioButtonSelected = "country";
        component.barChartChild = false;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionTerminal(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "terminals",
                type: "bar",
                data: [
                    {
                        value: 0.021531153353862464,
                        name: "Estonia",
                        nbUsers: 50,
                        usageTime: 120,
                        rawValue: 2.6913942471146584,
                        unit: "mol H+ eq",
                    },
                    {
                        value: "1",
                        name: "France",
                        nbUsers: 521,
                        usageTime: 1139.4721689059502,
                        rawValue: 157.2206110842526,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
        expect(echartsOption.xAxis).toEqual([
            { type: "category", data: ["Estonia", "France"] },
        ]);
    });

    it("should generate valid EChartsOption for Terminals (type case)", () => {
        const barChartData: DigitalServiceTerminalsImpact[] =
            digitalServicesService.transformTerminalData(
                require("mock-server/data/digital-service-data/digital_service_terminals_footprint.json"),
            );
        component.selectedCriteria = "acidification";
        component.terminalsRadioButtonSelected = "type";
        component.barChartChild = false;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionTerminal(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "terminals",
                type: "bar",
                data: [
                    {
                        value: "1",
                        name: "Desktop",
                        nbUsers: 450,
                        usageTime: 1293.3333333333333,
                        rawValue: 157.1190961971879,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.01639388015610166,
                        name: "Laptop",
                        nbUsers: 21,
                        usageTime: 365,
                        rawValue: 2.0492349676787853,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.005949393307673745,
                        name: "Tv box / decoder",
                        nbUsers: 100,
                        usageTime: 100,
                        rawValue: 0.7436741665005684,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
        expect(echartsOption.xAxis).toEqual([
            {
                type: "category",
                data: ["Desktop", "Laptop", "Tv box / decoder"],
            },
        ]);
    });

    it("should generate valid EChartsOption for Terminals Child (type case)", () => {
        const barChartData: DigitalServiceTerminalsImpact[] =
            digitalServicesService.transformTerminalData(
                require("mock-server/data/digital-service-data/digital_service_terminals_footprint.json"),
            );
        component.selectedCriteria = "acidification";
        component.terminalsRadioButtonSelected = "type";
        component.selectedDetailParam = "Laptop";
        component.barChartChild = true;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionTerminal(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "terminals",
                type: "bar",
                data: [
                    {
                        value: 0.015151364728808403,
                        rawValue: 1.8939205408096313,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.0007935588946565986,
                        rawValue: 0.09919486194849014,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.0002599900763016194,
                        rawValue: 0.03249875828623772,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.00018896645633503795,
                        rawValue: 0.023620806634426117,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
        expect(echartsOption.xAxis).toEqual([
            {
                type: "category",
                data: LifeCycleUtils.getLifeCycleList(),
            },
        ]);
    });

    it("should generate valid EChartsOption for Terminals Child (country case)", () => {
        const barChartData: DigitalServiceTerminalsImpact[] =
            digitalServicesService.transformTerminalData(
                require("mock-server/data/digital-service-data/digital_service_terminals_footprint.json"),
            );
        component.selectedCriteria = "acidification";
        component.terminalsRadioButtonSelected = "country";
        component.selectedDetailParam = "France";
        component.barChartChild = true;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionTerminal(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "terminals",
                type: "bar",
                data: [
                    {
                        value: 1.0752224563620985,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        rawValue: 134.4028114080429,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.06498291179013904,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        rawValue: 8.122863572090864,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.06556420828565024,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        rawValue: 8.19552568718791,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.05199528380762786,
                        name: undefined,
                        nbUsers: undefined,
                        usageTime: undefined,
                        rawValue: 6.499410416930914,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
        expect(echartsOption.xAxis).toEqual([
            {
                type: "category",
                data: LifeCycleUtils.getLifeCycleList(),
            },
        ]);
    });

    it("should generate valid EChartsOption for Servers", () => {
        const barChartData: DigitalServiceServersImpact[] = require("mock-server/data/digital-service-data/digital_service_servers_footprint.json");
        component.selectedCriteria = "acidification";
        component.barChartChild = false;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionServer(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "Server A",
                data: [
                    [
                        "digital-services-servers.server-type.Dedicated-Storage",
                        0.016719065246038937,
                        3211.715221903284,
                        "mol H+ eq",
                    ],
                ],
                type: "bar",
                stack: "Ad",
                emphasis: Object({ focus: "series" }),
                itemStyle: Object({ color: "rgb(0,178,255)" }),
            },
            {
                name: "Server C",
                data: [
                    [
                        "digital-services-servers.server-type.Dedicated-Storage",
                        0.016719065246038937,
                        3211.715221903284,
                        "mol H+ eq",
                    ],
                ],
                type: "bar",
                stack: "Ad",
                emphasis: Object({ focus: "series" }),
                itemStyle: Object({ color: "rgb(255,189,0)" }),
            },
            {
                name: "Server B",
                data: [
                    [
                        "digital-services-servers.server-type.Shared-Compute",
                        0.6209409557758594,
                        3211.715221903284,
                        "mol H+ eq",
                    ],
                ],
                type: "bar",
                stack: "Ad",
                emphasis: Object({ focus: "series" }),
                itemStyle: Object({ color: "#00B2FF" }),
            },
        ]);
    });

    it("should generate valid EChartsOption for Servers Child (case lifecycle)", () => {
        const barChartData: DigitalServiceServersImpact[] = require("mock-server/data/digital-service-data/digital_service_servers_footprint.json");
        component.selectedCriteria = "acidification";
        component.selectedDetailParam =
            "digital-services-servers.server-type.Shared-Compute";
        component.selectedDetailName = "Server B";
        component.serversRadioButtonSelected = "lifecycle";
        component.barChartChild = true;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionServerChild(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "servers",
                type: "bar",
                data: [
                    {
                        value: 0.37939472556712334,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.0036639668990612407,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.23510612851362,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                    {
                        value: 0.002776134796054795,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                    },
                ],
            },
        ]);
    });

    it("should generate valid EChartsOption for Servers Child (case vm)", () => {
        const barChartData: DigitalServiceServersImpact[] = require("mock-server/data/digital-service-data/digital_service_servers_footprint.json");
        component.selectedCriteria = "acidification";
        component.selectedDetailParam =
            "digital-services-servers.server-type.Shared-Compute";
        component.selectedDetailName = "Server B";
        component.serversRadioButtonSelected = "vm";
        component.barChartChild = true;

        const echartsOption: EChartsOption =
            component.loadStackBarOptionServerChild(barChartData);

        expect(echartsOption).toBeTruthy();
        expect(echartsOption.series).toBeTruthy();
        expect(echartsOption.series).toEqual([
            {
                name: "servers",
                type: "bar",
                data: [
                    {
                        value: 0.6209409557758594,
                        rawValue: 3211.715221903284,
                        unit: "mol H+ eq",
                        quantity: 4,
                    },
                ],
            },
        ]);
    });
});
