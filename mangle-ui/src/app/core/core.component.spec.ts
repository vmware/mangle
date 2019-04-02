import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { CoreComponent } from './core.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { CoreService } from './core.service';
import { of } from 'rxjs';

describe('CoreComponent', () => {
  let component: CoreComponent;
  let coreService: CoreService;
  let fixture: ComponentFixture<CoreComponent>; 

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientModule,
        RouterTestingModule.withRoutes([{ path: 'core', component: CoreComponent }])
      ],
      declarations: [ CoreComponent ],
      providers: [
        CoreService
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({"name":"user@mangle.local"}));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get user details', () => {
    component.getMyDetails();
    expect(component.user).toBe("user@mangle.local");
    expect(coreService.getMyDetails).toHaveBeenCalled();
  });

});
