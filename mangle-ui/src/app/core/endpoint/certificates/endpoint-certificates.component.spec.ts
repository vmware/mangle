import { ComponentFixture, TestBed } from '@angular/core/testing';

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
import { EndpointCertificatesComponent } from './endpoint-certificates.component';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { LoginComponent } from 'src/app/auth/login/login.component';

describe('EndpointCertificatesComponent', () => {
  let component: EndpointCertificatesComponent;
  let coreService: CoreService;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<EndpointCertificatesComponent>;

  let cert_data = { "id": null, "name": null };
  let cert_data_id = { "id": "some_id", "name": "name1" };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'endpoint-certificates', component: EndpointCertificatesComponent }, { path: 'login', component: LoginComponent }])
      ],
      declarations: [EndpointCertificatesComponent, CoreComponent, LoginComponent],
      providers: [
        CoreService,
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getCertificates').and.returnValue(of([]));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.callThrough().and.returnValue(of({ "name": "user@mangle.local" }));
    fixture = TestBed.createComponent(EndpointCertificatesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }
  );

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get certificates', () => {
    component.getCertificates();
    expect(endpointService.getCertificates).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
  });

  it('should populate certificate form', () => {
    component.populateCertificateForm(cert_data_id);
    expect(component.certificatesFormData.id).toBe("some_id");
  });

  it('should add update docker certificates', () => {
    //add certificates
    spyOn(endpointService, 'addDockerCertificates').and.returnValue(of({}));
    component.addUpdateDockerCertificates(cert_data);
    expect(endpointService.addDockerCertificates).toHaveBeenCalled();
    expect(endpointService.getCertificates).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);

    //update certificates
    spyOn(endpointService, 'updateDockerCertificates').and.returnValue(of({}));
    component.addUpdateDockerCertificates(cert_data_id);
    expect(endpointService.updateDockerCertificates).toHaveBeenCalled();
    expect(endpointService.getCertificates).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
  });

  it('should delete certificates', () => {
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    spyOn(endpointService, 'deleteCertificates').and.returnValue(of({}));
    component.deleteCertificates(cert_data_id);
    expect(endpointService.deleteCertificates).toHaveBeenCalled();
    expect(endpointService.getCertificates).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
  });

});
