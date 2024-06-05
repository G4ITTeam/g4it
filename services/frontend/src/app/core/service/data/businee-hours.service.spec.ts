import { TestBed } from '@angular/core/testing';

import { BusineeHoursService } from './businee-hours.service';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

describe('BusineeHoursService', () => {
  let service: BusineeHoursService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [BusineeHoursService],
    });
    service = TestBed.inject(BusineeHoursService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
