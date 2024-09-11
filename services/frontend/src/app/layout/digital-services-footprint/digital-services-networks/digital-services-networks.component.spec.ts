/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */
import { ComponentFixture, TestBed } from "@angular/core/testing";

import { HttpClientTestingModule } from "@angular/common/http/testing";
import { CUSTOM_ELEMENTS_SCHEMA } from "@angular/core";
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { SidebarModule } from "primeng/sidebar";
import { TableModule } from "primeng/table";
import { of } from "rxjs";
import {
    DigitalService,
    DigitalServiceNetworkConfig,
} from "src/app/core/interfaces/digital-service.interfaces";
import { UserService } from "src/app/core/service/business/user.service";
import { DigitalServicesDataService } from "src/app/core/service/data/digital-services-data.service";
import { SharedModule } from "src/app/core/shared/shared.module";
import { DigitalServicesNetworksComponent } from "./digital-services-networks.component";

describe("DigitalServicesNetworksComponent", () => {
    let component: DigitalServicesNetworksComponent;
    let fixture: ComponentFixture<DigitalServicesNetworksComponent>;

    //mock data service
    const digitalServiceDataMock = {
        digitalService$: of({
            name: "Test Digital Service",
            uid: "test-uid",
            creationDate: Date.now(),
            lastUpdateDate: Date.now(),
            lastCalculationDate: null,
            terminals: [],
            servers: [],
            networks: [],
        } as DigitalService),
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            declarations: [DigitalServicesNetworksComponent],
            imports: [
                SharedModule,
                TableModule,
                SidebarModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
            ],
            providers: [
                TranslatePipe,
                TranslateService,
                MessageService,
                UserService,
                {
                    provide: DigitalServicesDataService,
                    useValue: digitalServiceDataMock,
                },
            ],
            schemas: [CUSTOM_ELEMENTS_SCHEMA],
        });
        fixture = TestBed.createComponent(DigitalServicesNetworksComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it("should create", () => {
        expect(component).toBeTruthy();
    });

    it("should reset network", () => {
        //mock data
        component.networkTypes = [
            {
                code: "fixed-line-network-1",
                value: "Fixed FR",
            },
        ];

        //expected terminal after call
        var expectedNetwork = {
            uid: undefined,
            type: {
                code: "fixed-line-network-1",
                value: "Fixed FR",
            },
            yearlyQuantityOfGbExchanged: 0,
        };

        //call function
        component.resetNetwork();
        expect(component.network).toEqual(expectedNetwork);
    });

    it("should set the Network", () => {
        var testNetwork: DigitalServiceNetworkConfig = {
            uid: "randomUID",
            creationDate: 1700746167.59006,
            type: {
                code: "mobile-fix",
                value: "Mobile",
            },
            yearlyQuantityOfGbExchanged: 17,
            idFront: 0,
        };

        component.setNetworks(testNetwork, 0);

        expect(component.network).toEqual(testNetwork);
    });
});
