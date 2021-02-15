import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { EndpointService } from '../endpoint.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { CoreComponent } from '../../core.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { DatabaseComponent } from './database.component';

describe('DatabaseComponent', () => {
  let component: DatabaseComponent;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<DatabaseComponent>;

  let ep_data: any = { "id": null, "name": "db_test", "endPointType": "DATABASE", "credentialsName": "db_test", "databaseConnectionProperties": { "parentEndpointName": "do_test" }, "tags": { "admin_test": "admin_test" }, "enable": true };
  let ep_data_id: any = { "id": "with_id", "name": "db_test", "endPointType": "DATABASE", "credentialsName": "db_test", "databaseConnectionProperties": { "parentEndpointName": "do_test" }, "tags": { "admin_test": "admin_test" }, "enable": true };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientTestingModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'database', component: DatabaseComponent }])
      ],
      declarations: [DatabaseComponent, CoreComponent],
      providers: [
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getEndpoints').and.returnValue(of({ "content": [ep_data] }));
    fixture = TestBed.createComponent(DatabaseComponent);
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
    expect(component.endpoints[0].name).toBe("db_test");
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should add or update endpoint', () => {
    //add endpoint
    spyOn(endpointService, 'addEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data);
    expect(component.alertMessage).toBeTruthy();
    expect(component.endpoints[0].name).toBe("db_test");
    expect(endpointService.addEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
    //update endpoint
    spyOn(endpointService, 'updateEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data_id);
    expect(component.alertMessage).toBeTruthy();
    expect(component.endpoints[0].name).toBe("db_test");
    expect(endpointService.updateEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should delete endpoint', () => {
    spyOn(endpointService, 'deleteEndpoint').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteEndpoint(ep_data.name);
    expect(component.alertMessage).toBeTruthy();
    expect(endpointService.deleteEndpoint).toHaveBeenCalled();
  });

  it('should test endpoint connection', () => {
    spyOn(endpointService, 'testEndpointConnection').and.returnValue(of(ep_data));
    component.testEndpointConnection(true, ep_data);
    expect(component.alertMessage).toBeTruthy();
    expect(component.disableSubmit).toBe(false);
    expect(endpointService.testEndpointConnection).toHaveBeenCalled();
  });

  it('should test getParentEndpointByName', () => {
    component.allEndpoints = [];
    component.allEndpoints.push(ep_data_id)
    component.getParentEndpointByName(ep_data_id);
    expect(component.parentEndpoint).toBeDefined();
  });

  it('should test setParentEndpointNameVal', () => {
    component.allEndpoints = [];
    component.epFormData = ep_data;
    component.allEndpoints.push(ep_data_id)
    component.setParentEndpointNameVal(ep_data_id.name);
    expect(component.tagsData).toBeDefined();
  });

});
