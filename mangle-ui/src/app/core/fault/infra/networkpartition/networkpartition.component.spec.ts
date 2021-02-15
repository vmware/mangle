import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { NetworkPartitionComponent } from './networkpartition.component';
import { FaultService } from '../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('NetworkPartitionComponent', () => {
    let component: NetworkPartitionComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<NetworkPartitionComponent>;
    let router: Router;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                BrowserAnimationsModule,
                BrowserModule,
                FormsModule,
                HttpClientTestingModule,
                CommonModule,
                ClarityModule,
                RouterTestingModule.withRoutes([{ path: 'network-partition', component: NetworkPartitionComponent }])
            ],
            declarations: [NetworkPartitionComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(NetworkPartitionComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        spyOn(endpointService, 'getDockerContainers').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeNetworkPartitionFault').and.returnValue(of({ "taskData": { "schedule": null } }));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute Network Partition fault', () => {
        component.host = "10.2.3.5";
        component.executeNetworkPartitionFault(component.faultFormData);
        expect(faultService.executeNetworkPartitionFault).toHaveBeenCalled();
    });

    it('should execute getDockerContainers', () => {
        component.getDockerContainers("DOCKER", '');
        expect(endpointService.getDockerContainers).toHaveBeenCalled();
    });

    it('should execute updateHostList', () => {
        component.host = "10.2.3.6";
        component.updateHostList();
        expect(component.hostList.length).toBeGreaterThan(0);
    });

    it('should execute removeHost', () => {
        const node = "10.2.3.4";
        component.host = node;
        component.updateHostList();
        component.host = "10.2.3.5";
        component.updateHostList();
        component.removeHost(node);
        expect(component.hostList.length).toBeGreaterThan(0);
    });
});
