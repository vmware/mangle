import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DockerComponent } from './docker.component';
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
import { CoreComponent } from '../../core.component';

describe('DockerComponent', () => {
  let component: DockerComponent;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<DockerComponent>;

  let ep_data: any = { "id": null, "name": "docker_ep", "endPointType": "DOCKER", "dockerConnectionProperties": { "dockerHostname": "0.0.0.0", "dockerPort": "2375", "tlsEnabled": false } };
  let ep_data_id: any = { "id": "with_id", "name": "docker_ep", "endPointType": "DOCKER", "dockerConnectionProperties": { "dockerHostname": "0.0.0.0", "dockerPort": "2375", "tlsEnabled": false } };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'docker', component: DockerComponent }])
      ],
      declarations: [DockerComponent, CoreComponent],
      providers: [
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getEndpoints').and.returnValue(of([ep_data]));
    fixture = TestBed.createComponent(DockerComponent);
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
    expect(component.endpoints[0].name).toBe("docker_ep");
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should add or update endpoint', () => {
    //add endpoint
    spyOn(endpointService, 'addEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data);
    expect(component.successAlertMessage).toBeTruthy();
    expect(component.endpoints[0].name).toBe("docker_ep");
    expect(endpointService.addEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
    //update endpoint
    spyOn(endpointService, 'updateEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data_id);
    expect(component.successAlertMessage).toBeTruthy();
    expect(component.endpoints[0].name).toBe("docker_ep");
    expect(endpointService.updateEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should delete endpoint', () => {
    spyOn(endpointService, 'deleteEndpoint').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteEndpoint(ep_data.name);
    expect(component.successAlertMessage).toBeTruthy();
    expect(endpointService.deleteEndpoint).toHaveBeenCalled();
  });

  it('should test endpoint connection', () => {
    spyOn(endpointService, 'testEndpointConnection').and.returnValue(of(ep_data));
    component.testEndpointConnection(true, ep_data);
    expect(component.successAlertMessage).toBeTruthy();
    expect(component.disableSubmit).toBe(false);
    expect(endpointService.testEndpointConnection).toHaveBeenCalled();
  });

  it('should get certificates', () => {
    spyOn(endpointService, 'getCertificates').and.returnValue(of([]));
    component.getCertificates();
    expect(endpointService.getCertificates).toHaveBeenCalled();
  });

});
