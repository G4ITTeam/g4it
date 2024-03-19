/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { CardModule } from "primeng/card";
import { TooltipModule } from "primeng/tooltip";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { DatacenterStatsComponent } from "./datacenter-stats.component";

describe("DatacenterStatsComponent", () => {
    let component: DatacenterStatsComponent;
    let fixture: ComponentFixture<DatacenterStatsComponent>;
    let footprintService: FootprintDataService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [DatacenterStatsComponent],
            imports: [
                HttpClientTestingModule,
                CardModule,
                TooltipModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService, FootprintDataService],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        }).compileComponents();

        fixture = TestBed.createComponent(DatacenterStatsComponent);
        footprintService = TestBed.inject(FootprintDataService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });
});
