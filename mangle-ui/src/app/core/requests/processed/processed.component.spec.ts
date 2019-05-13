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
import { ProcessedComponent } from './processed.component';
import { LoginComponent } from 'src/app/auth/login/login.component';
import { CoreComponent } from 'src/app/core/core.component';
import { CoreService } from 'src/app/core/core.service';
import { RequestsService } from '../requests.service';

describe('ProcessedComponent', () => {
    let component: ProcessedComponent;
    let requestsService: RequestsService;
    let coreService: CoreService;
    let fixture: ComponentFixture<ProcessedComponent>;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [
                BrowserAnimationsModule,
                BrowserModule,
                FormsModule,
                HttpClientModule,
                CommonModule,
                ClarityModule,
                RouterTestingModule.withRoutes([{ path: 'processed', component: ProcessedComponent }, { path: 'login', component: LoginComponent }])
            ],
            declarations: [ProcessedComponent, CoreComponent, LoginComponent],
            providers: [
                RequestsService,
                CoreService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();
        coreService = TestBed.get(CoreService);
        spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
        requestsService = TestBed.get(RequestsService);
        spyOn(requestsService, 'getAllTasks').and.returnValue(of([]));
        fixture = TestBed.createComponent(ProcessedComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should get all processed tasks', () => {
        component.getAllProcessedTasks();
        expect(requestsService.getAllTasks).toHaveBeenCalled();
    });

});
