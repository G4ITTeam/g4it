import { ComponentFixture, TestBed } from "@angular/core/testing";

import { CommonEditorComponent } from "./common-editor.component";

import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { SharedModule } from "src/app/core/shared/shared.module";
import { CommonModule } from "@angular/common";
import { ButtonModule } from "primeng/button";
import { ConfirmationService, MessageService } from "primeng/api";

describe("CommonEditorComponent", () => {
    let component: CommonEditorComponent;
    let fixture: ComponentFixture<CommonEditorComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                SharedModule,
                TranslateModule.forRoot(),
                CommonModule,
                ButtonModule,
                TranslateModule.forRoot(),
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
            providers: [
                TranslatePipe,
                TranslateService,
                ConfirmationService,
                MessageService,
                {
                    useValue: {
                        sanitize: () => "safeString",
                    },
                },
            ],
        }).compileComponents();
        fixture = TestBed.createComponent(CommonEditorComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should remove style", () => {
        expect(component.removeStylesFromText('')).toBe('');
        expect(component.removeStylesFromText('<p>  </p>')).toBe('  ');
        expect(component.removeStylesFromText('<p>  </p><p>  </p>')).toBe('    ');
    })
});
