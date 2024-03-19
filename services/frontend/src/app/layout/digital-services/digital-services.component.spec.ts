/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { ActivatedRoute, Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ButtonModule } from "primeng/button";
import { CardModule } from "primeng/card";
import { ConfirmPopupModule } from "primeng/confirmpopup";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { ToastModule } from "primeng/toast";
import { of } from "rxjs";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { DigitalServicesComponent } from "./digital-services.component";
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { InplaceModule } from "primeng/inplace";
import { FormsModule } from "@angular/forms";
import { SharedModule } from "src/app/core/shared/shared.module";

describe("DigitalServicesComponent", () => {
    let component: DigitalServicesComponent;
    let fixture: ComponentFixture<DigitalServicesComponent>;
    let digitalServicesDataStub: any;
    let routerStub: any;

    beforeEach(async () => {
        // Fake digital services data service
        digitalServicesDataStub = jasmine.createSpyObj("DigitalServicesDataService", [
            "create",
            "list",
        ]);
        digitalServicesDataStub.create.and.returnValue(
            of({
                uid: "234567",
                name: "Digital Service#1",
                lastUpdateDate: Date.now(),
                creationDate: Date.now(),
            })
        );
        digitalServicesDataStub.list.and.returnValue(
            of([
                {
                    uid: "123456",
                    name: "Digital Service#1",
                    lastUpdateDate: Date.now(),
                    creationDate: Date.now(),
                },
                {
                    uid: "234567",
                    name: "Digital Service#2",
                    lastUpdateDate: Date.now(),
                    creationDate: Date.now(),
                },
            ])
        );

        // Fake router
        routerStub = jasmine.createSpyObj("Router", ["navigateByUrl"]);

        await TestBed.configureTestingModule({
            declarations: [DigitalServicesComponent],
            imports: [
                HttpClientTestingModule,
                RouterTestingModule,
                InplaceModule,
                FormsModule,
                SharedModule,
                ConfirmPopupModule,
                TranslateModule.forRoot(),
                ButtonModule,
                ToastModule,
                CardModule,
                ScrollPanelModule,
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: {},
                        },
                    },
                },
                MessageService,
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServicesDataStub,
                },
                {
                    provide: Router,
                    useValue: routerStub,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(DigitalServicesComponent);
        component = fixture.componentInstance;
        await fixture.whenStable();
        fixture.detectChanges();
    });

    it("should list all digital services on init", async () => {
        expect(digitalServicesDataStub.list).toHaveBeenCalledTimes(1);
        expect(component.digitalServices).toHaveSize(2);
        expect(component.digitalServices.some((el) => el.uid === "234567")).toBeTrue();
    });
});
