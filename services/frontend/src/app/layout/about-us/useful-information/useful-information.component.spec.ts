import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsefulInformationComponent } from './useful-information.component';

describe('UsefulInformationComponent', () => {
  let component: UsefulInformationComponent;
  let fixture: ComponentFixture<UsefulInformationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsefulInformationComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UsefulInformationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
