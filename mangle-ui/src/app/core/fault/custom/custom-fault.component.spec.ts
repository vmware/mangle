import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { Router } from '@angular/router';
import { CustomFaultComponent } from './custom-fault.component';
import { FaultService } from '../fault.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { EndpointService } from '../../endpoint/endpoint.service';

describe('CustomFaultComponent', () => {
    let component: CustomFaultComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<CustomFaultComponent>;
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
                RouterTestingModule.withRoutes([{ path: 'custom-fault', component: CustomFaultComponent }])
            ],
            declarations: [CustomFaultComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(CustomFaultComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeCustomFault').and.returnValue(of({ "taskData": { "schedule": null } }));
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute custom fault', () => {
        component.executeCustomFault(component.faultFormData);
        expect(faultService.executeCustomFault).toHaveBeenCalled();
    });

});
