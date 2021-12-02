import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { FaultService } from '../../../fault.service';
import { DrainK8SNodeComponent } from './drain-k8s-nodes.component';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DrainK8SNodeComponent', () => {
    let component: DrainK8SNodeComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<DrainK8SNodeComponent>;
    let router: Router;

    let k8s_data: any = {
        "endpointName": "endpointName",
        "resourceName": "resourceName",
        "resourceLabels": { "resource1": "resource1" },
        "injectionHomeDir": null,
        "randomInjection": true
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                BrowserAnimationsModule,
                BrowserModule,
                FormsModule,
                HttpClientTestingModule,
                CommonModule,
                ClarityModule,
                RouterTestingModule.withRoutes([{ path: 'drain-k8s-nodes', component: DrainK8SNodeComponent }])
            ],
            declarations: [DrainK8SNodeComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(DrainK8SNodeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeK8SDrainNodeFault').and.returnValue(of([k8s_data]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute K8S drain nodes fault', () => {
        component.resourceLabelsData = { "resource1": "resource1" };
        component.executeK8SDrainNodeFault(k8s_data);
        expect(faultService.executeK8SDrainNodeFault).toHaveBeenCalled();
    });

});
