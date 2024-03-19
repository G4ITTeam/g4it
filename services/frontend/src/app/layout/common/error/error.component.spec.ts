/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpStatusCode } from "@angular/common/http";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { ActivatedRoute } from "@angular/router";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { from, of } from "rxjs";
import { ErrorComponent } from "./error.component";

class MockTranslateService {
    get(key: string) {
        return of(key);
    }
}

describe("ErrorComponent", () => {
    let component: ErrorComponent;
    let fixture: ComponentFixture<ErrorComponent>;

    let translateService: TranslateService;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ErrorComponent],
            imports: [BrowserAnimationsModule, TranslateModule.forRoot()],
            providers: [
                {
                    provide: ActivatedRoute,
                    useValue: {
                        params: from([{ err: HttpStatusCode.ServiceUnavailable }]),
                    },
                },
                { provide: TranslateService, useClass: MockTranslateService },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(ErrorComponent);

        translateService = TestBed.inject(TranslateService);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should set errorTitle and errorText for service-unavailable", () => {
        spyOn(translateService, "get").and.returnValue(of("Service Unavailable"));
        component.ngOnInit();

        expect(translateService.get).toHaveBeenCalledWith(
            "error-page.title.service-unavailable"
        );
        expect(translateService.get).toHaveBeenCalledWith(
            "error-page.text.service-unavailable"
        );
        expect(component.errorTitle).toBe("Service Unavailable");
    });

    it("should set errorTitle and errorText for access-denied", () => {
        spyOn(translateService, "get").and.returnValue(of("Access Denied"));
        const activatedRoute = TestBed.inject(ActivatedRoute);
        activatedRoute.params = from([{ err: HttpStatusCode.Forbidden }]);
        component.ngOnInit();

        expect(translateService.get).toHaveBeenCalledWith(
            "error-page.title.access-denied"
        );
        expect(translateService.get).toHaveBeenCalledWith(
            "error-page.text.access-denied"
        );
        expect(component.errorTitle).toBe("Access Denied");
    });

    it("should unsubscribe on component destruction", () => {
        spyOn(component.ngUnsubscribe, "next");
        spyOn(component.ngUnsubscribe, "complete");

        fixture.destroy();

        expect(component.ngUnsubscribe.next).toHaveBeenCalled();
        expect(component.ngUnsubscribe.complete).toHaveBeenCalled();
    });
});
