/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { TooltipModule } from "primeng/tooltip";
import { MonthYearPipe } from "src/app/core/pipes/monthyear.pipe";
import { BatchStatusComponent } from "./batch-status.component";

describe("BatchStatusComponent", () => {
    let component: BatchStatusComponent;
    let fixture: ComponentFixture<BatchStatusComponent>;
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [BatchStatusComponent, MonthYearPipe],
            imports: [TooltipModule, TranslateModule.forRoot()],
            providers: [TranslatePipe, TranslateService],
        }).compileComponents();

        fixture = TestBed.createComponent(BatchStatusComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should choose the good class and tootip with batchStatus as UKNOWN", () => {
        component.batchStatusCode = "UNKNOWN";
        component.ngOnInit();
        expect(component.cssClass).toBe("pi pi-spin pi-spinner icon-running");
        expect(component.toolTip).toBe("Running");
    });

    it("should choose the good class and tootip with batchStatus as COMPLETED", () => {
        component.batchStatusCode = "COMPLETED";
        component.ngOnInit();
        expect(component.cssClass).toBe("pi pi-check icon-completed");
        expect(component.toolTip).toBe("Completed");
    });

    it("should choose the good class and tootip with batchStatus as FAILED", () => {
        component.batchStatusCode = "FAILED";
        component.ngOnInit();
        expect(component.cssClass).toBe("pi pi-times icon-failed");
        expect(component.toolTip).toBe("Failed");
    });

    it("should choose the good class and tootip with batchStatus as COMPLETED_WITH_ERRORS", () => {
        component.batchStatusCode = "COMPLETED_WITH_ERRORS";
        component.ngOnInit();
        expect(component.cssClass).toBe("icon-completed-with-errors");
        expect(component.toolTip).toBe("Completed with errors");
        expect(component.betweenDiv).toBe("!");
    });

    it("should choose the good class and tootip with batchStatus as SKIPPED", () => {
        component.batchStatusCode = "SKIPPED";
        component.ngOnInit();
        expect(component.cssClass).toBe("icon-completed-with-errors");
        expect(component.toolTip).toBe("Completed with errors");
        expect(component.betweenDiv).toBe("!");
    });
});
