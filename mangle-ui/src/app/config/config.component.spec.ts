import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ConfigComponent } from './config.component';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { HttpClientModule, HttpResponse } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { ConfigService } from './config.service';
import { of } from 'rxjs';
import { Router } from '@angular/router';

describe('ConfigComponent', () => {
  let component: ConfigComponent;
  let configService: ConfigService;
  let router: Router;
  let fixture: ComponentFixture<ConfigComponent>;

  let config_data: any = { "username": "admin@mangle.local", "oldPassword": "abc", "password": "abc", "rePassword": "abc" };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        FormsModule,
        HttpClientModule,
        RouterTestingModule.withRoutes([{ path: 'config', component: ConfigComponent }])
      ],
      declarations: [ConfigComponent],
      providers: [
        ConfigService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(ConfigComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    configService = TestBed.get(ConfigService);
    spyOn(configService, 'updateLocalUserConfig').and.returnValue(of(new HttpResponse<any>({ status: 200, body: {} })));
    router = TestBed.get(Router);
    spyOn(router, 'navigateByUrl');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should update password', () => {
    component.updatePassword(config_data);
    expect(component.errorFlag).toBe(false);
    expect(configService.updateLocalUserConfig).toHaveBeenCalled();
  });

});
