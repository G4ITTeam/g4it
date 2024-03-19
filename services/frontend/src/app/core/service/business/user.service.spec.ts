/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { TestBed } from "@angular/core/testing";

import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { Router } from "@angular/router";
import { RouterTestingModule } from "@angular/router/testing";
import { Role } from "../../interfaces/roles.interfaces";
import { Organization, Subscriber, User } from "../../interfaces/user.interfaces";
import { UserService } from "./user.service";
import { MessageService } from "primeng/api";
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { ToastModule } from "primeng/toast";

describe("UserService", () => {
    let httpMock: HttpTestingController;
    let service: UserService;
    let router: Router;
    let userMock: User = {
        username: "prenom.nom@soprasteria.com",
        subscribers: [
            {
                name: "SSG",
                defaultFlag: true,
                organizations: [
                    {
                        name: "SSG",
                        defaultFlag: false,
                        roles: [Role.DigitalServiceRead,Role.DigitalServiceWrite],
                    },
                    {
                        name: "G4IT",
                        defaultFlag: true,
                        roles: [Role.InventoryRead, Role.DigitalServiceRead,Role.InventoryWrite,Role.DigitalServiceWrite],
                    },
                ],
            },
            {
                name: "PasSSG",
                defaultFlag: false,
                organizations: [
                    {
                        name: "123",
                        defaultFlag: true,
                        roles: [Role.DigitalServiceRead,Role.DigitalServiceWrite],
                    },
                    {
                        name: "456",
                        defaultFlag: false,
                        roles: [Role.InventoryRead,Role.InventoryWrite],
                    },
                ],
            },
        ],
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                RouterTestingModule,
                HttpClientTestingModule,
                TranslateModule.forRoot(),
                ToastModule,
            ],
            providers: [UserService, MessageService, TranslateService],
        });
        service = TestBed.inject(UserService);
        router = TestBed.inject(Router);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be create", () => {
        expect(UserService).toBeTruthy();
    });

    it("should check if allowed and redirect if necessary", () => {
        spyOn(service, "setUserSubscription");
        spyOn(service, "redirectToAllowedPage");

        const existingSubscriber: Subscriber = userMock.subscribers[0];
        const existingOrganization: Organization =
            userMock.subscribers[0].organizations[1];

        service.checkIfAllowedElseRedirect(existingSubscriber, existingOrganization);

        expect(service.setUserSubscription).toHaveBeenCalledWith(
            existingSubscriber,
            existingOrganization
        );
        expect(service.redirectToAllowedPage).toHaveBeenCalledWith(
            existingSubscriber.name,
            existingOrganization
        );
    });

    it("should redirect to inventories based on roles", () => {
        spyOn(service["router"], "navigateByUrl");

        const subscriber = "SSG";
        const organization: Organization = userMock.subscribers[0].organizations[1];

        service.redirectToAllowedPage(subscriber, organization);

        expect(service["router"].navigateByUrl).toHaveBeenCalledWith(
            `${subscriber}/${organization.name}/inventories`
        );
    });

    it("should redirect to digital-services based on roles", () => {
        spyOn(service["router"], "navigateByUrl");

        const subscriber = "SSG";
        const organization: Organization = userMock.subscribers[1].organizations[0];

        service.redirectToAllowedPage(subscriber, organization);

        expect(service["router"].navigateByUrl).toHaveBeenCalledWith(
            `${subscriber}/${organization.name}/digital-services`
        );
    });

    it("should return a valid hex color code for a given string", () => {
        const inputString = "example";

        const result = service.generateColor(inputString);

        expect(result).toMatch(/^#[0-9A-F]{6}$/i);
    });

    afterEach(() => {
        TestBed.resetTestingModule();
    });

    it("should return the same color for the same input string", () => {
        const inputString = "example";

        const result1 = service.generateColor(inputString);
        const result2 = service.generateColor(inputString);

        expect(result1).toEqual(result2);
    });
});
