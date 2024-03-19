/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { CheckboxModule } from "primeng/checkbox";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { OverlayModule } from "primeng/overlay";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { TabMenuModule } from "primeng/tabmenu";
import { TabViewModule } from "primeng/tabview";
import { ToastModule } from "primeng/toast";
import { from } from "rxjs";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { InventoriesCritereFootprintComponent } from "./critere/inventories-critere-footprint.component";
import { DatavizFilterComponent } from "./dataviz-filter/dataviz-filter.component";
import { InventoriesGlobalFootprintComponent } from "./global/inventories-global-footprint.component";
import { InventoriesHeaderFootprintComponent } from "./header/inventories-header-footprint.component";
import { InventoriesFootprintComponent } from "./inventories-footprint.component";
import { MessageService } from "primeng/api";

describe("InventoriesFootprintComponent", () => {
    let component: InventoriesFootprintComponent;
    let fixture: ComponentFixture<InventoriesFootprintComponent>;
    let inventoryDate: "05-2023";
    let router: Router;
    let footprintService: FootprintDataService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [
                InventoriesFootprintComponent,
                InventoriesHeaderFootprintComponent,
                InventoriesGlobalFootprintComponent,
                InventoriesCritereFootprintComponent,
                DatavizFilterComponent,
            ],
            imports: [
                SharedModule,
                ButtonModule,
                ToastModule,
                TabMenuModule,
                TabViewModule,
                ScrollPanelModule,
                ConfirmPopupModule,
                CardModule,
                OverlayModule,
                CheckboxModule,
                HttpClientTestingModule,
                RouterTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                FootprintDataService,
                MessageService,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: {
                                get: () => inventoryDate,
                            },
                        },
                    },
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(InventoriesFootprintComponent);
        router = TestBed.inject(Router);
        footprintService = TestBed.inject(FootprintDataService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it('should call updateMainChartData() when selectedCriteria is "multi-criteria"', () => {
        spyOn(component, "updateMainChartData");
        component.selectedCriteria = "multi-criteria";
        component.updateCharts();
        expect(component.updateMainChartData).toHaveBeenCalled();
    });

    it('should call updateDonutChartData() when selectedCriteria is not "multi-criteria"', () => {
        spyOn(component, "updateDonutChartData");
        component.selectedCriteria = "particulate-matter";
        component.updateCharts();
        expect(component.updateDonutChartData).toHaveBeenCalled();
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
