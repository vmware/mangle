import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginComponent } from './login.component';
import { FormsModule } from '@angular/forms';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { AuthService } from '../auth.service';
import { HttpResponse } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { ClarityModule, ClrLoadingState } from '@clr/angular';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let authService: AuthService;
  let router: Router;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserModule,
        BrowserAnimationsModule,
        ClarityModule,
        FormsModule,
        HttpClientTestingModule,
        RouterTestingModule.withRoutes([{ path: 'login', component: LoginComponent }])
      ],
      declarations: [LoginComponent],
      providers: [
        AuthService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    authService = TestBed.get(AuthService);
    router = TestBed.get(Router);
    spyOn(authService, 'getAuthSources').and.returnValue(of({"content": ["mangle.local"] }));
    spyOn(authService, 'login').and.returnValue(of(new HttpResponse<any>({ status: 200, body: {} })));
    spyOn(router, 'navigateByUrl');
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get auth sources', () => {
    component.getAuthSources();
    expect(component.authSources[0]).toBe("mangle.local");
  });

  it('should login', () => {
    component.login({});
    expect(component.submitBtnState).toBe(ClrLoadingState.DEFAULT);
    expect(authService.login).toHaveBeenCalled();
  });

});
