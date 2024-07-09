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
import { TranslateModule, TranslateService } from "@ngx-translate/core";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { Role } from "../../interfaces/roles.interfaces";
import { Organization, Subscriber } from "../../interfaces/user.interfaces";
import { UserService } from "./user.service";

describe("UserService", () => {
    let httpMock: HttpTestingController;
    let service: UserService;
    let router: Router;

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

    it("should check if subscriber admin is allowed to see page admnistration ", () => {
        const subscriber = {
            roles: [Role.SubscriberAdmin],
        } as Subscriber;

        const organization = {} as Organization;

        var result = service.checkIfAllowed(subscriber, organization, "administration");

        expect(result).toBeTrue();
    });

    it("should check if organization admin is allowed to see page admnistration ", () => {
        const subscriber = {
            roles: [Role.DigitalServiceRead],
        } as Subscriber;

        const organization = {
            roles: [Role.OrganizationAdmin],
        } as Organization;

        var result = service.checkIfAllowed(subscriber, organization, "administration");

        expect(result).toBeTrue();
    });

    it("should check if read only user is allowed to see page admnistration ", () => {
        const subscriber = {
            roles: [Role.DigitalServiceRead],
        } as Subscriber;

        const organization = {
            roles: [Role.DigitalServiceRead],
        } as Organization;

        var result = service.checkIfAllowed(subscriber, organization, "administration");

        expect(result).toBeFalse();
    });

    it("should check if subscriber administrator is allowed to see page inventories and digital-services page", () => {
        const subscriber = {
            roles: [Role.SubscriberAdmin],
        } as Subscriber;

        const organization = {
            roles: [] as any,
        } as Organization;

        var resultIS = service.checkIfAllowed(subscriber, organization, "inventories");
        var resultDS = service.checkIfAllowed(
            subscriber,
            organization,
            "digital-services",
        );

        expect(resultIS).toBeTrue();
        expect(resultDS).toBeTrue();
    });
});
