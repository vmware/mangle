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
import { DeleteK8SResourceComponent } from './delete-k8s-resource.component';
import { FaultService } from '../../../fault.service';
import { Router } from '@angular/router';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DeleteK8SResourceComponent', () => {
    let component: DeleteK8SResourceComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<DeleteK8SResourceComponent>;
    let router: Router;

    let k8s_data: any = {
        "endpointName": "endpointName",
        "resourceType": "NODE",
        "resourceName": "resourceName",
        "resourceLabels": { "resource1": "resource1" },
        "randomInjection": false,
        "injectionHomeDir": "/tmp"
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
                RouterTestingModule.withRoutes([{ path: 'delete-k8s-resource', component: DeleteK8SResourceComponent }])
            ],
            declarations: [DeleteK8SResourceComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(DeleteK8SResourceComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeK8SDeleteResourceFault').and.returnValue(of([k8s_data]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute K8S delete resource fault', () => {
        component.resourceLabelsData = { "resource1": "resource1" };
        component.executeK8SDeleteResourceFault(k8s_data);
        expect(faultService.executeK8SDeleteResourceFault).toHaveBeenCalled();
    });

});
