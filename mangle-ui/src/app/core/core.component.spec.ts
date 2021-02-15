import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CoreComponent } from './core.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { RouterTestingModule } from '@angular/router/testing';
import { CoreService } from './core.service';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('CoreComponent', () => {
  let component: CoreComponent;
  let coreService: CoreService;
  let fixture: ComponentFixture<CoreComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([{ path: 'core', component: CoreComponent }])
      ],
      declarations: [CoreComponent],
      providers: [
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(CoreComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local", "roleNames": ["ROLE_READONLY"] }));
    spyOn(coreService, 'getMyRolesAndPrivileges').and.returnValue(of({ "content":  [{ "name": "ROLE_READONLY", "privilegeNames": ["READONLY"] }] }));
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
