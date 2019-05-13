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
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { VcenterNicComponent } from './vcenter-nic.component';
import { FaultService } from '../../../fault.service';
import { Router } from '@angular/router';

describe('VcenterNicComponent', () => {
    let component: VcenterNicComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<VcenterNicComponent>;
    let router: Router;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                BrowserAnimationsModule,
                BrowserModule,
                FormsModule,
                HttpClientModule,
                CommonModule,
                ClarityModule,
                RouterTestingModule.withRoutes([{ path: 'vcenter-nic', component: VcenterNicComponent }])
            ],
            declarations: [VcenterNicComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(VcenterNicComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeVcenterNicFault').and.returnValue(of([component.faultFormData]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    beforeEach(() => {

    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute vcenter nic fault', () => {
        component.executeVcenterNicFault(component.faultFormData);
        expect(faultService.executeVcenterNicFault).toHaveBeenCalled();
    });

});
