import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { CoreService } from 'src/app/core/core.service';
import { CoreComponent } from 'src/app/core/core.component';
import { EndpointCredentialsComponent } from './endpoint-credentials.component';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { LoginComponent } from 'src/app/auth/login/login.component';

describe('EndpointCredentialsComponent', () => {
  let component: EndpointCredentialsComponent;
  let coreService: CoreService;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<EndpointCredentialsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'endpoint-credentials', component: EndpointCredentialsComponent }, { path: 'login', component: LoginComponent }])
      ],
      declarations: [EndpointCredentialsComponent, CoreComponent, LoginComponent],
      providers: [
        CoreService,
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getCredentials').and.callThrough().and.returnValue(of([]));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.callThrough().and.returnValue(of({ "name": "user@mangle.local" }));
    fixture = TestBed.createComponent(EndpointCredentialsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
