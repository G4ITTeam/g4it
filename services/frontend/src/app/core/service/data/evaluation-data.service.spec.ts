/*
 * G4IT
 * Copyright 2023 Sopra Steria
 *
 * This product includes software developed by
 * French Ecological Ministery (https://gitlab-forge.din.developpement-durable.gouv.fr/pub/numeco/m4g/numecoeval)
 */ 
import {
    HttpClientTestingModule,
    HttpTestingController,
} from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { EvaluationDataService } from "./evaluation-data.service";

describe("EvaluationDataService", () => {
    let httpMock: HttpTestingController;
    let evaluationDataService: EvaluationDataService;
    let organization: string = "SSG";
    let inventoryDate = 3;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [EvaluationDataService],
        });
        evaluationDataService = TestBed.inject(EvaluationDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(evaluationDataService).toBeTruthy();
    });

    it("launchEstimation() should work", () => {
        evaluationDataService.launchEstimation(inventoryDate, organization).subscribe();

        const req = httpMock.expectOne(`inventories/${inventoryDate}/evaluation`);
        expect(req.request.method).toEqual("POST");
        req.flush("inventoryDate");

        httpMock.verify();
    });
});
