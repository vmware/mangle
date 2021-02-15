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
import { K8SResourceNotReadyComponent } from './k8s-resource-not-ready.component';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('K8SResourceNotReadyComponent', () => {
    let component: K8SResourceNotReadyComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<K8SResourceNotReadyComponent>;
    let router: Router;

    let k8s_data: any = {
        "endpointName": "endpointName",
        "resourceName": "resourceName",
        "resourceLabels": { "resource1": "resource1" },
        "appContainerName": "appContainerName",
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
                RouterTestingModule.withRoutes([{ path: 'k8s-resource-not-ready', component: K8SResourceNotReadyComponent }])
            ],
            declarations: [K8SResourceNotReadyComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(K8SResourceNotReadyComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeK8SResourceNotReadyFault').and.returnValue(of([k8s_data]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute K8S resource not ready fault', () => {
        component.resourceLabelsData = { "resource1": "resource1" };
        component.executeK8SResourceNotReadyFault(k8s_data);
        expect(faultService.executeK8SResourceNotReadyFault).toHaveBeenCalled();
    });

});
