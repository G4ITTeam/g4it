import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { Constants } from "src/constants";
import { AdministrationDataService } from "./administration-data-service";

describe("AdministrationDataService", () => {
    let service: AdministrationDataService;
    let httpMock: HttpTestingController;
    let organizationId = 1;
    let searchName = "sop";
    let subscriberId = 1;
    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [AdministrationDataService],
        });
        service = TestBed.inject(AdministrationDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(service).toBeTruthy();
    });

    it("should have getOrganizationData function", () => {
        expect(service.getOrganizations).toBeTruthy();
    });

    it("should have getOrganizationData function", () => {
        expect(service.getUsers).toBeTruthy();
    });

    it("should delete a organization", () => {
        const updateJson = {
            subscriberId: 1,
            name: "DEMO",
            status: Constants.ORGANIZATION_STATUSES.TO_BE_DELETED,
            dataRetentionDay: 7,
        };
        service.updateOrganization(updateJson.subscriberId, updateJson).subscribe();

        const req = httpMock.expectOne(`administrator/organizations?organizationId=1`);
        expect(req.request.method).toEqual("PUT");

        httpMock.verify();
    });

    it("should revert deleted organization", () => {
        const updateJson = {
            subscriberId: 1,
            name: "DEMO",
            status: Constants.ORGANIZATION_STATUSES.ACTIVE,
            dataRetentionDay: null,
        };
        service.updateOrganization(updateJson.subscriberId, updateJson).subscribe();

        const req = httpMock.expectOne(`administrator/organizations?organizationId=1`);
        expect(req.request.method).toEqual("PUT");

        httpMock.verify();
    });

    it("should get User Details", () => {
        service.getUserDetails(organizationId).subscribe();
        const req = httpMock.expectOne(
            `administrator/organizations/users?organizationId=1`,
        );
        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should get user Search Deatails", () => {
        service.getSearchDetails(searchName, subscriberId, organizationId).subscribe();
        const req = httpMock.expectOne(
            `administrator/subscribers/users?searchedName=${searchName}&subscriberId=${subscriberId}&organizationId=${organizationId}`,
        );
        expect(req.request.method).toEqual("GET");

        httpMock.verify();
    });

    it("should save organization", () => {
        const body = {
            organizationId: organizationId,
            users: [
                {
                    userId: 1,
                    roles: [
                        "ROLE_INVENTORY_READ",
                        "ROLE_INVENTORY_WRITE",
                        "ROLE_DIGITAL_SERVICE_READ",
                        "ROLE_DIGITAL_SERVICE_WRITE",
                        "ROLE_ORGANIZATION_ADMINISTRATOR",
                    ],
                },
            ],
        };
        service.postUserToOrganizationAndAddRoles(body).subscribe();

        const req = httpMock.expectOne(`administrator/organizations/users`);
        expect(req.request.method).toEqual("POST");

        httpMock.verify();
    });

    it("should delete organization", () => {
        const body = {
            organizationId: organizationId,
            users: [
                {
                    userId: 1,
                    roles: [
                        "ROLE_INVENTORY_READ",
                        "ROLE_INVENTORY_WRITE",
                        "ROLE_DIGITAL_SERVICE_READ",
                        "ROLE_DIGITAL_SERVICE_WRITE",
                        "ROLE_ORGANIZATION_ADMINISTRATOR",
                    ],
                },
            ],
        };
        service.deleteUserDetails(body).subscribe();

        const req = httpMock.expectOne(`administrator/organizations/users`);
        expect(req.request.method).toEqual("DELETE");

        httpMock.verify();
    });
});
