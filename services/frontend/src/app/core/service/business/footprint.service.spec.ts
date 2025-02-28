import { HttpClientTestingModule } from "@angular/common/http/testing";
import { TestBed } from "@angular/core/testing";
import { TranslateModule } from "@ngx-translate/core";
import { ApplicationImpact } from "../../interfaces/footprint.interface";
import { OutApplicationsRest } from "../../interfaces/output.interface";
import { FootprintService } from "./footprint.service";

describe("FootprintService", () => {
    let service: FootprintService;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule, TranslateModule.forRoot()],
        });
        service = TestBed.inject(FootprintService);
    });

    describe("getTransformOutApplications", () => {
        it("should handle empty input array", () => {
            const outApplications: OutApplicationsRest[] = [];
            const expected: ApplicationImpact[] = [];
            const result = service.getTransformOutApplications(outApplications);
            expect(result).toEqual(expected);
        });
    });
});
