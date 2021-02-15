import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
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
import { HttpClientTestingModule } from '@angular/common/http/testing';

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
                HttpClientTestingModule,
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
        spyOn(coreService, 'getMyDetails').and.returnValue(of({ content: { "name": "user@mangle.local" } }));
        requestsService = TestBed.get(RequestsService);
        spyOn(requestsService, 'getAllTasksBasedOnIndex').and.returnValue(of({ "content": { "taskList": [], "taskSize": 0 } }));
        fixture = TestBed.createComponent(ProcessedComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should get page processed tasks', () => {
        var filterOnMock: any = {
            "taskType": "",
            "taskDescription": "",
            "taskStatus": "",
            "fromIndex": 0,
            "toIndex": 9
        };
        component.getPageProcessedTasks(filterOnMock);
        expect(requestsService.getAllTasksBasedOnIndex).toHaveBeenCalled();
    });

});
