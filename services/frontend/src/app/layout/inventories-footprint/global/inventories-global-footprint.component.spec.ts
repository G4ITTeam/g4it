/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { EChartsOption } from "echarts";
import { NGX_ECHARTS_CONFIG, NgxEchartsModule } from "ngx-echarts";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { SharedModule } from "src/app/core/shared/shared.module";
import { InformationCardComponent } from "../../common/information-card/information-card.component";
import { DatacenterStatsComponent } from "../datacenter-stats/datacenter-stats.component";
import { ImpactAggregateInfosComponent } from "../impact-aggregate-infos/impact-aggregate-infos.component";
import { PhysicalequipmentStatsComponent } from "../physicalequipment-stats/physicalequipment-stats.component";
import { InventoriesGlobalFootprintComponent } from "./inventories-global-footprint.component";

describe("InventoriesGlobalFootprintComponent", () => {
    let component: InventoriesGlobalFootprintComponent;
    let fixture: ComponentFixture<InventoriesGlobalFootprintComponent>;
    let template: HTMLElement;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                InventoriesGlobalFootprintComponent,
                MonthYearPipe,
                DatacenterStatsComponent,
                PhysicalequipmentStatsComponent,
                InformationCardComponent,
                ImpactAggregateInfosComponent
            ],
            imports: [
                ButtonModule,
                CardModule,
                HttpClientTestingModule,
                SharedModule,
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
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InventoriesGlobalFootprintComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
        template = fixture.nativeElement;
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

    describe("updateEchartsOptions", () => {
        it("should generate the correct options", () => {
            const selectedView = "country";
            const echartsData = [
                {
                    data: "France",
                    impacts: [
                        {
                            critere: "acidification",
                            fis: 5,
                            impactUnitaire: 42,
                            unite: "unit1",
                        },
                        {
                            critere: "ionising-radiation",
                            fis: 10,
                            impactUnitaire: 12,
                            unite: "unit2",
                        },
                    ],
                },
                {
                    data: "England",
                    impacts: [
                        {
                            critere: "acidification",
                            fis: 15,
                            impactUnitaire: 22,
                            unite: "unit3",
                        },
                        {
                            critere: "ionising-radiation",
                            fis: 20,
                            impactUnitaire: 35,
                            unite: "unit4",
                        },
                    ],
                },
            ];

            const expectedOptions: EChartsOption = {
                angleAxis: {
                    type: "category",
                    data: ["acidification", "ionising-radiation"],
                },
            };

            component.updateEchartsOptions(selectedView, echartsData);

            expect(component.options.angleAxis).toEqual(expectedOptions.angleAxis);
        });
    });
});
