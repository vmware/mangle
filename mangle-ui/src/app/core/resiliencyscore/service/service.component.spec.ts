import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { ResiliencyscoreService } from '../resiliencyscore.service';
import { ServiceComponent } from './service.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ServiceComponent', () => {
    let component: ServiceComponent;
    let resiliencyScoreService: ResiliencyscoreService;
    let fixture: ComponentFixture<ServiceComponent>;

    let serviceDefaultValue: any = {
        "name": "defaultService",
        "queryNames": ["high-cpu-usage", "high-memory-usage"],
        "tags": null
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
                RouterTestingModule.withRoutes([{ path: 'service', component: ServiceComponent }])
            ],
            declarations: [ServiceComponent],
            providers: [
                ResiliencyscoreService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .compileComponents();
        resiliencyScoreService = TestBed.get(ResiliencyscoreService);
        spyOn(resiliencyScoreService, 'getAllServices').and.returnValue(of([serviceDefaultValue]));
        fixture = TestBed.createComponent(ServiceComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create ServiceComponent Component', () => {
        expect(component).toBeTruthy();
    });

    it('should populate Service form', () => {
        component.populateServiceForm({});
        expect(component.disableSubmit).toBe(false);
    });

    it('should add Service Definition', () => {
        spyOn(resiliencyScoreService, 'addService').and.returnValue(of(serviceDefaultValue));
        component.addOrUpdateService(serviceDefaultValue, 'Add');
        expect(component.alertMessage).toBeTruthy();
        expect(resiliencyScoreService.addService).toHaveBeenCalled();
        expect(resiliencyScoreService.getAllServices).toHaveBeenCalled();
    });

    it('should edit Service Definition', () => {
        spyOn(resiliencyScoreService, 'updateService').and.returnValue(of(serviceDefaultValue));
        component.addOrUpdateService(serviceDefaultValue, 'Edit');
        expect(component.alertMessage).toBeTruthy();
        expect(resiliencyScoreService.updateService).toHaveBeenCalled();
        expect(resiliencyScoreService.getAllServices).toHaveBeenCalled();
    });

    it('should delete Service', () => {
        spyOn(resiliencyScoreService, 'deleteService').and.returnValue(of({}));
        spyOn(window, 'confirm').and.callFake(function () { return true; });
        component.deleteService(serviceDefaultValue.name);
        expect(component.alertMessage).toBeTruthy();
        expect(resiliencyScoreService.deleteService).toHaveBeenCalled();
    });

});
