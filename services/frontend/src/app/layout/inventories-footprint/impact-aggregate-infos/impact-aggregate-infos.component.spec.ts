/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CardModule } from "primeng/card";

import { DebugElement } from "@angular/core";
import { By } from "@angular/platform-browser";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ImpactAggregateInfosComponent } from "./impact-aggregate-infos.component";
import { DecimalsPipe } from "src/app/core/pipes/decimal.pipe";
import { IntegerPipe } from "src/app/core/pipes/integer.pipe";

describe("ImpactAggregateInfosComponent", () => {
    let component: ImpactAggregateInfosComponent;
    let fixture: ComponentFixture<ImpactAggregateInfosComponent>;
    let el: DebugElement;

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [ImpactAggregateInfosComponent, DecimalsPipe, IntegerPipe],
            imports: [CardModule, TranslateModule.forRoot()],
            providers: [TranslatePipe, TranslateService],
        });
        fixture = TestBed.createComponent(ImpactAggregateInfosComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
        el = fixture.debugElement;
        const content = el.query(By.css(".critere-number div div")).nativeElement;
        expect(content.textContent).toContain("--");
    });

    it("should display < 1 when displayValue is < 1", async () => {
        // When the displayValue is set to a 0 < displayValue < 1
        component.selectedUnit = "Peopleeq";
        component.displayValue = 0.1;
        fixture.detectChanges();

        el = fixture.debugElement;
        const content = el.query(By.css(".critere-number div div")).nativeElement;
        expect(content.textContent).toContain("< 1");
    });

    it("should display value when displayValue is >= 1", async () => {
        // When the displayValue is set to displayValue >= 1
        component.displayValue = 1;
        fixture.detectChanges();

        el = fixture.debugElement;
        const content = el.query(By.css(".critere-number div div")).nativeElement;
        expect(content.textContent).toContain("1");
    });

    it("should display -- when displayValue is 0", async () => {
        // First set to 3,
        component.displayValue = 3;
        fixture.detectChanges();

        el = fixture.debugElement;
        let content = el.query(By.css(".critere-number div div")).nativeElement;
        expect(content.textContent).toContain("3");

        // Then to 0 to test change
        component.displayValue = 0;
        fixture.detectChanges();

        el = fixture.debugElement;
        content = el.query(By.css(".critere-number div div")).nativeElement;
        expect(content.textContent).toContain("--");
    });

    it("should set class round-button-selected on first button when selectedUnit is impact", async () => {
        component.selectedUnit = "impact";
        fixture.detectChanges();

        el = fixture.debugElement;
        const firstButton = el.query(
            By.css(".round-button-container button:nth-of-type(1)")
        ).nativeElement;
        expect(firstButton).toHaveClass("round-button-selected");
        const secondButton = el.query(
            By.css(".round-button-container button:nth-of-type(2)")
        ).nativeElement;
        expect(secondButton).toHaveClass("round-button");
    });

    it("should set class round-button on first button when selectedUnit is people eq", async () => {
        component.selectedUnit = "Peopleeq";
        fixture.detectChanges();

        el = fixture.debugElement;
        const firstButton = el.query(
            By.css(".round-button-container button:nth-of-type(1)")
        ).nativeElement;
        expect(firstButton).toHaveClass("round-button");
        const secondButton = el.query(
            By.css(".round-button-container button:nth-of-type(2)")
        ).nativeElement;
        expect(secondButton).toHaveClass("round-button-selected");
    });

    it("should display text when selectedCriteria is set", async () => {
        component.selectedCriteria = "my criteria";
        fixture.detectChanges();

        el = fixture.debugElement;
        const content = el.query(By.css(".critere-text")).nativeElement;
        expect(content.textContent).toContain("my criteria");
    });

    it("should emit event when criteria selected is clicked", async () => {
        spyOn(component.selectedUnitChange, "emit");
        el = fixture.debugElement;
        const firstButton = el.query(
            By.css(".round-button-container button:nth-of-type(1)")
        ).nativeElement;
        firstButton.click();

        fixture.detectChanges();
        expect(component.selectedUnitChange.emit).toHaveBeenCalledWith("impact");

        const secondButton = el.query(
            By.css(".round-button-container button:nth-of-type(2)")
        ).nativeElement;
        secondButton.click();

        fixture.detectChanges();
        expect(component.selectedUnitChange.emit).toHaveBeenCalledWith("Peopleeq");
    });
});
