import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ResiliencyScoreMetricConfigComponent } from './resiliencyscore-metric-config.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { SettingService } from '../setting.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('ResiliencyScoreMetricConfigComponent', () => {
    let component: ResiliencyScoreMetricConfigComponent;
    let rscoreService: SettingService;
    let fixture: ComponentFixture<ResiliencyScoreMetricConfigComponent>;

    let resiliencyconfig_metric_config_data: any = {
        'name': 'default-config', 'metricName': 'mangle.resiliency.score', 'metricSource': 'mangle', 'testReferenceWindow': 15, 'resiliencyCalculationWindow': 1, 'metricQueryGranularity': ''
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
                RouterTestingModule.withRoutes([{ path: 'resiliencyscore-metric-config', component: ResiliencyScoreMetricConfigComponent }])
            ],
            declarations: [ResiliencyScoreMetricConfigComponent],
            providers: [
                SettingService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .compileComponents();
        rscoreService = TestBed.get(SettingService);
        spyOn(rscoreService, 'getResiliencyScoreMetricConfig').and.returnValue(of([resiliencyconfig_metric_config_data]));
        fixture = TestBed.createComponent(ResiliencyScoreMetricConfigComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create ResiliencyScoreMetricConfigComponent Component', () => {
        expect(component).toBeTruthy();
    });

    it('should populate ResiliencyScore Metric Config form', () => {
        component.populateMetricConfigForm({});
        expect(component.disableSubmit).toBe(false);
    });

    it('should add or update ResiliencyScore Metric Configuration', () => {
        //Validate adding ResiliencyScore Metric Config
        spyOn(rscoreService, 'addResiliencyScoreMetricConfig').and.returnValue(of(resiliencyconfig_metric_config_data));
        component.addOrUpdateMetricConfig(resiliencyconfig_metric_config_data, 'Add');
        expect(component.alertMessage).toBeTruthy();
        expect(component.metricConfigInfo[0].name).toBe('default-config');
        expect(rscoreService.addResiliencyScoreMetricConfig).toHaveBeenCalled();
        expect(rscoreService.getResiliencyScoreMetricConfig).toHaveBeenCalled();
        //Validate editing ResiliencyScore Metric Config
        spyOn(rscoreService, 'updateResiliencyScoreMetricConfig').and.returnValue(of(resiliencyconfig_metric_config_data));
        component.addOrUpdateMetricConfig(resiliencyconfig_metric_config_data, 'Edit');
        expect(component.alertMessage).toBeTruthy();
        expect(component.metricConfigInfo[0].name).toBe('default-config');
        expect(rscoreService.updateResiliencyScoreMetricConfig).toHaveBeenCalled();
        expect(rscoreService.getResiliencyScoreMetricConfig).toHaveBeenCalled();
    });

    it('should delete ResiliencyScore Metric Config', () => {
        spyOn(rscoreService, 'deleteResiliencyScoreMetricConfig').and.returnValue(of({}));
        spyOn(window, 'confirm').and.callFake(function () { return true; });
        component.deleteMetricConfig(resiliencyconfig_metric_config_data.name);
        expect(component.alertMessage).toBeTruthy();
        expect(rscoreService.deleteResiliencyScoreMetricConfig).toHaveBeenCalled();
    });

});
