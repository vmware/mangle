import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PasswordComponent } from './password.component';
import { RouterTestingModule } from '@angular/router/testing';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { CoreService } from 'src/app/core/core.service';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { SettingService } from 'src/app/setting/setting.service';
import { AuthService } from '../auth.service';
import { of } from 'rxjs';
import { Router } from '@angular/router';

describe('PasswordComponent', () => {
  let component: PasswordComponent;
  let coreService: CoreService;
  let settingService: SettingService;
  let authService: AuthService;
  let router: Router;
  let fixture: ComponentFixture<PasswordComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientModule,
        RouterTestingModule.withRoutes([{ path: 'password', component: PasswordComponent }])
      ],
      declarations: [PasswordComponent],
      providers: [
        CoreService,
        SettingService,
        AuthService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(PasswordComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    coreService = TestBed.get(CoreService);
    settingService = TestBed.get(SettingService);
    authService = TestBed.get(AuthService);
    router = TestBed.get(Router);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
    spyOn(settingService, 'updatePassword').and.returnValue(of(component.passwordFormData));
    spyOn(authService, 'logout').and.returnValue(of({}));
    spyOn(router, 'navigateByUrl');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update password', () => {
    component.updatePassword(component.passwordFormData);
    expect(component.successFlag).toBe(true);
    expect(settingService.updatePassword).toHaveBeenCalled();
  });

  it('should login again', () => {
    component.loginAgain();
    expect(router.navigateByUrl).toHaveBeenCalled();
  });

});
