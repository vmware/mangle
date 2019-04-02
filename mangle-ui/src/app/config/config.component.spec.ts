import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigComponent } from './config.component';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from '../auth/auth.service';
import { SettingService } from '../setting/setting.service';
import { ConfigService } from './config.service';
import { of } from 'rxjs';

describe('ConfigComponent', () => {
  let component: ConfigComponent;
  let authService: AuthService;
  let settingService: SettingService;
  let configService: ConfigService;
  let fixture: ComponentFixture<ConfigComponent>;

  let config_data: any = { "username": "admin@mangle.local", "oldPassword": "abc", "password": "abc", "rePassword": "abc" };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientModule,
        RouterTestingModule.withRoutes([{ path: 'config', component: ConfigComponent }])
      ],
      declarations: [ConfigComponent],
      providers: [
        AuthService,
        SettingService,
        ConfigService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    authService = TestBed.get(AuthService);
    settingService = TestBed.get(SettingService);
    configService = TestBed.get(ConfigService);
    spyOn(authService, 'login').and.returnValue(of(new HttpResponse<any>({ status: 200, body: {} })));
    spyOn(authService, 'logout').and.returnValue(of({}));
    spyOn(settingService, 'updateLocalUserConfig').and.returnValue(of(new HttpResponse<any>({ status: 200, body: {} })));
    spyOn(configService, 'setConfigStatus').and.returnValue(of({}));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update password', () => {
    component.updatePassword(config_data);
    expect(component.errorFlag).toBe(false);
    expect(settingService.updateLocalUserConfig).toHaveBeenCalled();
    expect(configService.setConfigStatus).toHaveBeenCalled();
    expect(authService.logout).toHaveBeenCalled();
  });

  it('should set config status', () => {
    component.setConfigStatus();
    expect(configService.setConfigStatus).toHaveBeenCalled();
  });

});
