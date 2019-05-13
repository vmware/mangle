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
import { EndpointCredentialsComponent } from './endpoint-credentials.component';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';

describe('EndpointCredentialsComponent', () => {
  let component: EndpointCredentialsComponent;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<EndpointCredentialsComponent>;

  let k8s_cred_data = { "id": null, "name": null };
  let k8s_cred_data_id = { "id": "some_id", "name": "name1" };
  let vcenter_cred_data = { "id": null, "name": null, "userName": null, "password": null };
  let vcenter_cred_data_id = { "id": "some_id", "name": "name1", "userName": "user1", "password": "" };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'endpoint-credentials', component: EndpointCredentialsComponent }])
      ],
      declarations: [EndpointCredentialsComponent],
      providers: [
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getCredentials').and.callThrough().and.returnValue(of([{ "name": "name1" }]));
    fixture = TestBed.createComponent(EndpointCredentialsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get credentials', () => {
    component.getCredentials();
    expect(endpointService.getCredentials).toHaveBeenCalled();
    expect(component.credentials[0].name).toBe("name1");
    expect(component.isLoading).toBe(false);
  });

  it('should populate credential form', () => {
    component.populateCredentialForm(vcenter_cred_data_id);
    expect(component.credentialFormData.id).toBe("some_id");
  });

  it('should show authorization', () => {
    component.showAuthorization("apassword");
    expect(component.passwordHidden).toBe(false);
    component.showAuthorization("privateKey");
    expect(component.privateKeyHidden).toBe(false);
  });

  it('should add update kubernetes credential', () => {
    //add kubernetes credential
    spyOn(endpointService, 'addk8sCredential').and.returnValue(of({}));
    component.addUpdateKubernetesCredential(k8s_cred_data);
    expect(endpointService.addk8sCredential).toHaveBeenCalled();
    expect(endpointService.getCredentials).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);

    //update kubernetes credential
    spyOn(endpointService, 'updatek8sCredential').and.returnValue(of({}));
    component.addUpdateKubernetesCredential(k8s_cred_data_id);
    expect(endpointService.updatek8sCredential).toHaveBeenCalled();
    expect(endpointService.getCredentials).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
  });

  it('should add update vcenter credential', () => {
    //add vcenter credential
    spyOn(endpointService, 'addVcenterCredential').and.returnValue(of({}));
    component.addUpdateVcenterCredential(vcenter_cred_data);
    expect(endpointService.addVcenterCredential).toHaveBeenCalled();
    expect(endpointService.getCredentials).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);

    //update vcenter credential
    spyOn(endpointService, 'updateVcenterCredential').and.returnValue(of({}));
    component.addUpdateVcenterCredential(vcenter_cred_data_id);
    expect(endpointService.updateVcenterCredential).toHaveBeenCalled();
    expect(endpointService.getCredentials).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
  });

  it('should delete credential', () => {
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    spyOn(endpointService, 'deleteCredential').and.returnValue(of({}));
    component.deleteCredential(vcenter_cred_data_id);
    expect(endpointService.deleteCredential).toHaveBeenCalled();
    expect(endpointService.getCredentials).toHaveBeenCalled();
    expect(component.isLoading).toBe(false);
  });

});
