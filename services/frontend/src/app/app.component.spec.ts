/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import { HttpClientTestingModule } from "@angular/common/http/testing";
import { ComponentFixture, TestBed } from "@angular/core/testing";
import { RouterTestingModule } from "@angular/router/testing";
import { MsalBroadcastService, MsalService } from "@azure/msal-angular";
import { EventMessage, InteractionStatus } from "@azure/msal-browser";
import { MessageService } from "primeng/api";
import { ToastModule } from "primeng/toast";
import { Subject } from "rxjs";
import { AppComponent } from "./app.component";
import { UserDataService } from "./core/service/data/user-data.service";

describe("AppComponent", () => {
    let component: AppComponent;
    let fixture: ComponentFixture<AppComponent>;
    let mockMsalBroadcastService: any;
    let mockAuthService: any;
    let userServiceStub: any;

    beforeEach(async () => {
        mockMsalBroadcastService = jasmine.createSpyObj("MsalBroadcastService", [
            "inProgress$",
        ]);

        const inProgress$ = new Subject<InteractionStatus>();
        mockMsalBroadcastService.inProgress$ = inProgress$.asObservable();

        const msalSubject$ = new Subject<EventMessage>();
        mockMsalBroadcastService.msalSubject$ = msalSubject$.asObservable();

        userServiceStub = jasmine.createSpyObj("UserDataService", ["fetchUserInfo"]);

        // Create a mock version of 'instance' for 'AuthService'
        mockAuthService = {
            instance: {
                getActiveAccount: jasmine
                    .createSpy("getActiveAccount")
                    .and.returnValue(null),
                getAllAccounts: jasmine.createSpy("getAllAccounts").and.returnValue([]),
                setActiveAccount: jasmine.createSpy("setActiveAccount"),
            },
        };

        TestBed.configureTestingModule({
            declarations: [AppComponent],
            imports: [ToastModule, HttpClientTestingModule, RouterTestingModule],
            providers: [
                MessageService,
                {
                    provide: MsalService,
                    useValue: jasmine.createSpyObj("MsalService", ["instance"]),
                },
                {
                    provide: MsalBroadcastService,
                    useValue: mockMsalBroadcastService,
                },
                {
                    provide: UserDataService,
                    useValue: userServiceStub,
                },
            ],
        }).compileComponents();

        fixture = TestBed.createComponent(AppComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        await fixture.whenStable();
        fixture.detectChanges();
    });

    afterEach(() => {
        fixture.destroy();
    });

    it("should create and fetch user infos", async () => {
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
