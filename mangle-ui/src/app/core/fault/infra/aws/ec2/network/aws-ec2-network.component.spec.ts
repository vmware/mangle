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
import { AwsEC2NetworkComponent } from './aws-ec2-network.component';
import { FaultService } from '../../../../fault.service';
import { Router } from '@angular/router';

describe('AwsEC2NetworkComponent', () => {
    let component: AwsEC2NetworkComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<AwsEC2NetworkComponent>;
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
                RouterTestingModule.withRoutes([{ path: 'aws-ec2-network', component: AwsEC2NetworkComponent }])
            ],
            declarations: [AwsEC2NetworkComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(AwsEC2NetworkComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeAwsEC2NetworkFault').and.returnValue(of([component.faultFormData]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute aws ec2 instance network fault', () => {
        component.executeAwsEC2NetworkFault(component.faultFormData);
        expect(faultService.executeAwsEC2NetworkFault).toHaveBeenCalled();
    });

});
