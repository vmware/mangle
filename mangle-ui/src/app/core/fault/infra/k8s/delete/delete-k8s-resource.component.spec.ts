import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { LoginComponent } from 'src/app/auth/login/login.component';
import { CoreComponent } from 'src/app/core/core.component';
import { CoreService } from 'src/app/core/core.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { RequestsService } from 'src/app/core/requests/requests.service';
import { ProcessedComponent } from 'src/app/core/requests/processed/processed.component';
import { DeleteK8SResourceComponent } from './delete-k8s-resource.component';
import { FaultService } from '../../../fault.service';

describe('DeleteK8SResourceComponent', () => {
    let component: DeleteK8SResourceComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let coreService: CoreService;
    let requestsService: RequestsService;
    let fixture: ComponentFixture<DeleteK8SResourceComponent>;

    beforeEach(async(() => {
        TestBed.configureTestingModule({
            imports: [
                BrowserAnimationsModule,
                BrowserModule,
                FormsModule,
                HttpClientModule,
                CommonModule,
                ClarityModule,
                RouterTestingModule.withRoutes([{ path: 'delete-k8s-resource', component: DeleteK8SResourceComponent }, { path: 'core/requests', component: ProcessedComponent }, { path: 'login', component: LoginComponent }])
            ],
            declarations: [DeleteK8SResourceComponent, CoreComponent, LoginComponent, ProcessedComponent],
            providers: [
                FaultService,
                CoreService,
                RequestsService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
    }));

    beforeEach(() => {
        coreService = TestBed.get(CoreService);
        spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        requestsService = TestBed.get(RequestsService);
        spyOn(requestsService, 'getAllTasks').and.returnValue(of([]));
        fixture = TestBed.createComponent(DeleteK8SResourceComponent);
        component = fixture.componentInstance;
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeK8SDeleteResourceFault').and.returnValue(of([component.faultFormData]));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute K8S delete resource fault', () => {
        component.executeK8SDeleteResourceFault(component.faultFormData);
        expect(faultService.executeK8SDeleteResourceFault).toHaveBeenCalled();
    });

});
