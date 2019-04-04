import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MachineComponent } from './machine.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { EndpointService } from '../endpoint.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { CoreService } from '../../core.service';
import { CoreComponent } from '../../core.component';
import { LoginComponent } from 'src/app/auth/login/login.component';

describe('MachineComponent', () => {
  let component: MachineComponent;
  let endpointService: EndpointService;
  let coreService: CoreService;
  let fixture: ComponentFixture<MachineComponent>;

  let ep_data: any = { "id": null, "name": "machine_ep", "endPointType": "MACHINE", "credentialsName": "machine_cred", "remoteMachineConnectionProperties": { "host": "0.0.0.0", "sshPort": 22, "timeout": 10000, "osType": "LINUX" } };
  let ep_data_id: any = { "id": "with_id", "name": "machine_ep", "endPointType": "MACHINE", "credentialsName": "machine_cred", "remoteMachineConnectionProperties": { "host": "0.0.0.0", "sshPort": 22, "timeout": 10000, "osType": "LINUX" } };
  let cred_data: any = { "name": "machine_cred", "username": "root", "password": "machine_pass", "privateKey": null };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'machine', component: MachineComponent }, { path: 'login', component: LoginComponent }])
      ],
      declarations: [MachineComponent, CoreComponent, LoginComponent],
      providers: [
        EndpointService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getEndpoints').and.returnValue(of([ep_data]));
    spyOn(endpointService, 'getCredentials').and.returnValue(of([cred_data]));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
    fixture = TestBed.createComponent(MachineComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should populate endpoint form', () => {
    component.populateEndpointForm({});
    expect(component.disableSubmit).toBe(true);
  });

  it('should get endpoints', () => {
    component.getEndpoints();
    expect(component.endpoints[0].name).toBe("machine_ep");
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should get credentials', () => {
    component.getCredentials();
    expect(component.credentials[0].name).toBe("machine_cred");
    expect(endpointService.getCredentials).toHaveBeenCalled();
  });

  it('should add or update endpoint', () => {
    //add endpoint
    spyOn(endpointService, 'addEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data);
    expect(component.successFlag).toBe(true);
    expect(component.endpoints[0].name).toBe("machine_ep");
    expect(endpointService.addEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
    //update endpoint
    spyOn(endpointService, 'updateEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data_id);
    expect(component.successFlag).toBe(true);
    expect(component.endpoints[0].name).toBe("machine_ep");
    expect(endpointService.updateEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should delete endpoint', () => {
    spyOn(endpointService, 'deleteEndpoint').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteEndpoint(ep_data.name);
    expect(component.successFlag).toBe(true);
    expect(endpointService.deleteEndpoint).toHaveBeenCalled();
  });

  it('should add machine credential', () => {
    spyOn(endpointService, 'addRemoteMachineCredential').and.returnValue(of(cred_data));
    component.addMachineCredential(cred_data);
    expect(component.successFlag).toBe(true);
    expect(endpointService.addRemoteMachineCredential).toHaveBeenCalled();
    expect(component.credentials[0].name).toBe("machine_cred");
    expect(endpointService.getCredentials).toHaveBeenCalled();
  });

  it('should test endpoint connection', () => {
    spyOn(endpointService, 'testEndpointConnection').and.returnValue(of(ep_data));
    component.testEndpointConnection(ep_data);
    expect(component.successFlag).toBe(true);
    expect(component.disableSubmit).toBe(false);
    expect(endpointService.testEndpointConnection).toHaveBeenCalled();
  });

});
