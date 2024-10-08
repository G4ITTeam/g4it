/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { FormsModule } from "@angular/forms";
import { ActivatedRoute } from "@angular/router";
import { InplaceModule } from "primeng/inplace";
import { InputTextModule } from "primeng/inputtext";
import { TabMenuModule } from "primeng/tabmenu";
import { of } from "rxjs";

import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { RouterTestingModule } from "@angular/router/testing";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { ScrollPanelModule } from "primeng/scrollpanel";
import { DigitalService } from "src/app/core/interfaces/digital-service.interfaces";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesFootprintComponent } from "./digital-services-footprint.component";

describe("DigitalServicesFootprintComponent", () => {
    let component: DigitalServicesFootprintComponent;
    let fixture: ComponentFixture<DigitalServicesFootprintComponent>;
    let digitalServiceDataStub: any;
    const uid = "ds-uid";

    beforeEach(async () => {
        digitalServiceDataStub = jasmine.createSpyObj("DigitalServicesDataService", [
            "get",
            "update",
            "digitalService$",
        ]);
        digitalServiceDataStub.get.and.returnValue(
            of<DigitalService>({
                uid,
                name: "Digital Service #1",
                lastUpdateDate: Date.now(),
                creationDate: Date.now(),
                lastCalculationDate: null,
                networks: [],
                servers: [],
                terminals: [],
                members: [],
            }),
        );
        digitalServiceDataStub.update.and.returnValue(
            of<DigitalService>({
                uid,
                name: "Digital Service #2",
                lastUpdateDate: Date.now(),
                creationDate: Date.now(),
                lastCalculationDate: null,
                networks: [],
                servers: [],
                terminals: [],
                members: [],
            }),
        );
        digitalServiceDataStub.digitalService$.and.returnValue(
            of({
                name: "Test Digital Service",
                uid: "test-uid",
                creationDate: Date.now(),
                lastUpdateDate: Date.now(),
                lastCalculationDate: null,
                terminals: [],
                servers: [],
                networks: [],
                members: [],
            } as DigitalService),
        );

        TestBed.configureTestingModule({
            declarations: [DigitalServicesFootprintComponent],
            imports: [
                InplaceModule,
                FormsModule,
                InputTextModule,
                RouterTestingModule,
                TabMenuModule,
                SharedModule,
                ScrollPanelModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                {
                    provide: ActivatedRoute,
                    useValue: {
                        snapshot: {
                            paramMap: {
                                get: () => uid,
                            },
                        },
                    },
                },
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServiceDataStub,
                },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(DigitalServicesFootprintComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should fetch digital service infos from url params on init", async () => {
        await fixture.whenStable();
        fixture.detectChanges();
        expect(digitalServiceDataStub.get).toHaveBeenCalledOnceWith(uid);
    });

    it("should update digital service name", async () => {
        await fixture.whenStable();
        component.updateDigitalService();
        fixture.detectChanges();
        await fixture.whenStable();

        expect(digitalServiceDataStub.update).toHaveBeenCalledTimes(1);
        expect(component.digitalService.name).toBe("Digital Service #2");
    });
});
