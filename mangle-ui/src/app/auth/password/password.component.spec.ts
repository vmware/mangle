import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordComponent } from './password.component';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CoreService } from 'src/app/core/core.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { SettingService } from 'src/app/setting/setting.service';
import { AuthService } from '../auth.service';
import { of } from 'rxjs';
import { LoginComponent } from '../login/login.component';

describe('PasswordComponent', () => {
  let component: PasswordComponent;
  let coreService: CoreService;
  let settingService: SettingService;
  let authService: AuthService;
  let fixture: ComponentFixture<PasswordComponent>; 

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientModule,
        RouterTestingModule.withRoutes([{ path: 'password', component: PasswordComponent }, { path: 'login', component: LoginComponent }])
      ],
      declarations: [ PasswordComponent, LoginComponent ],
      providers: [
        CoreService,
        SettingService,
        AuthService
      ],
      schemas: [ NO_ERRORS_SCHEMA ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    coreService = TestBed.get(CoreService);
    settingService = TestBed.get(SettingService);
    authService = TestBed.get(AuthService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({"name":"user@mangle.local"}));
    spyOn(settingService, 'updateLocalUser').and.returnValue(of({}));
    spyOn(authService, 'logout').and.returnValue(of({}));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get user details', () => {
    component.getMyDetails();
    expect(component.passwordFormData.username).toBe("user@mangle.local");
    expect(coreService.getMyDetails).toHaveBeenCalled();
  });

  it('should update password', () => {
    component.updatePassword({});
    expect(component.successFlag).toBe(true);
    expect(settingService.updateLocalUser).toHaveBeenCalled();
  });

});
