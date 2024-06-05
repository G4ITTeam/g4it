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
import { CardModule } from "primeng/card";
import { TooltipModule } from "primeng/tooltip";
import { FootprintDataService } from "src/app/core/service/data/footprint-data.service";
import { PhysicalequipmentStatsComponent } from "./physicalequipment-stats.component";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";

describe("PhysicalequipmentStatsComponent", () => {
    let component: PhysicalequipmentStatsComponent;
    let fixture: ComponentFixture<PhysicalequipmentStatsComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [PhysicalequipmentStatsComponent, DecimalsPipe, IntegerPipe],
            imports: [
                CardModule,
                HttpClientTestingModule,
                TooltipModule,
                TranslateModule.forRoot(),
            ],
            providers: [TranslatePipe, TranslateService, FootprintDataService],
        }).compileComponents();

        fixture = TestBed.createComponent(PhysicalequipmentStatsComponent);
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
});
