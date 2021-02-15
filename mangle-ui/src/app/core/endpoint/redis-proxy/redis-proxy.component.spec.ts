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
import { RedisProxyComponent } from './redis-proxy.component';

describe('RedisProxyComponent', () => {
  let component: RedisProxyComponent;
  let endpointService: EndpointService;
  let fixture: ComponentFixture<RedisProxyComponent>;

  let ep_data: any = { "id": null, "name": "redis-proxy_ep", "endPointType": "REDIS_FI_PROXY", "redisProxyConnectionProperties": { "host": "0.0.0.0", "port": 6380 } };
  let ep_data_id: any = { "id": "with_id", "name": "redis-proxy_ep", "endPointType": "REDIS_FI_PROXY", "redisProxyConnectionProperties": { "host": "0.0.0.0", "port": 6380 } };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientTestingModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'redis-proxy', component: RedisProxyComponent }])
      ],
      declarations: [RedisProxyComponent, CoreComponent],
      providers: [
        EndpointService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
    endpointService = TestBed.get(EndpointService);
    spyOn(endpointService, 'getEndpoints').and.returnValue(of({ "content": [ep_data] }));
    fixture = TestBed.createComponent(RedisProxyComponent);
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
    expect(component.endpoints[0].name).toBe("redis-proxy_ep");
    expect(endpointService.getEndpoints).toHaveBeenCalled();
  });

  it('should add or update endpoint', () => {
    //add endpoint
    spyOn(endpointService, 'addEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data);
    expect(component.alertMessage).toBeTruthy();
    expect(component.endpoints[0].name).toBe("redis-proxy_ep");
    expect(endpointService.addEndpoint).toHaveBeenCalled();
    expect(endpointService.getEndpoints).toHaveBeenCalled();
    //update endpoint
    spyOn(endpointService, 'updateEndpoint').and.returnValue(of(ep_data_id));
    component.addOrUpdateEndpoint(ep_data_id);
    expect(component.alertMessage).toBeTruthy();
    expect(component.endpoints[0].name).toBe("redis-proxy_ep");
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

});
