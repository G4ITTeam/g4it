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
import { TranslateModule, TranslatePipe, TranslateService } from "@ngx-translate/core";
import { LoadingDataService } from "./loading-data.service";

describe("LoadingDataService", () => {
    let httpMock: HttpTestingController;
    let loadingService: LoadingDataService;
    let inventoryDate: any = 4;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
            providers: [LoadingDataService, TranslatePipe, TranslateService],
        });
        loadingService = TestBed.inject(LoadingDataService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    it("should be created", () => {
        expect(LoadingDataService).toBeTruthy();
    });
});
