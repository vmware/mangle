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
import { DockerStateChangeComponent } from './docker-state-change.component';
import { FaultService } from '../../../fault.service';
import { Router } from '@angular/router';

describe('DockerStateChangeComponent', () => {
    let component: DockerStateChangeComponent;
    let faultService: FaultService;
    let endpointService: EndpointService;
    let fixture: ComponentFixture<DockerStateChangeComponent>;
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
                RouterTestingModule.withRoutes([{ path: 'docker-state-change', component: DockerStateChangeComponent }])
            ],
            declarations: [DockerStateChangeComponent],
            providers: [
                FaultService,
                EndpointService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        fixture = TestBed.createComponent(DockerStateChangeComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
        endpointService = TestBed.get(EndpointService);
        spyOn(endpointService, 'getAllEndpoints').and.returnValue(of([]));
        spyOn(endpointService, 'getDockerContainers').and.returnValue(of([]));
        faultService = TestBed.get(FaultService);
        spyOn(faultService, 'executeDockerStateChangeFault').and.returnValue(of([component.faultFormData]));
        router = TestBed.get(Router);
        spyOn(router, 'navigateByUrl');
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should execute docker state change fault', () => {
        component.executeDockerStateChangeFault(component.faultFormData);
        expect(faultService.executeDockerStateChangeFault).toHaveBeenCalled();
    });

    it('should execute getDockerContainers', () => {
        component.getDockerContainers("DOCKER",'');
        expect(endpointService.getDockerContainers).toHaveBeenCalled();
    });

});
