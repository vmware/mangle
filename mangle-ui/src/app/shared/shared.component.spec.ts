import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SharedComponent } from './shared.component';
import { RouterTestingModule } from '@angular/router/testing';

describe('SharedComponent', () => {
  let component: SharedComponent;
  let fixture: ComponentFixture<SharedComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        RouterTestingModule.withRoutes([{ path: 'shared', component: SharedComponent }])
      ],
      declarations: [SharedComponent]
    })
      .compileComponents();
    fixture = TestBed.createComponent(SharedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
