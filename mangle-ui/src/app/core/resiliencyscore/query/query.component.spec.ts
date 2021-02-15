import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { QueryService } from './query.service';
import { QueryComponent } from './query.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { cpus } from 'os';

describe('QueryComponent', () => {
    let component: QueryComponent;
    let queryService: QueryService;
    let fixture: ComponentFixture<QueryComponent>;

    let queryDefaultValue: any = {
        "name": "defaultQuery",
        "weight": 0.8,
        "queryCondition": "ts(\"cpus.usage\") > 90"
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
                RouterTestingModule.withRoutes([{ path: 'quer', component: QueryComponent }])
            ],
            declarations: [QueryComponent],
            providers: [
                QueryService
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
            .compileComponents();
        queryService = TestBed.get(QueryService);
        spyOn(queryService, 'getAllQueries').and.returnValue(of([queryDefaultValue]));
        fixture = TestBed.createComponent(QueryComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create QueryComponent Component', () => {
        expect(component).toBeTruthy();
    });

    it('should populate Query form', () => {
        component.populateQueryForm({});
        expect(component.disableSubmit).toBe(false);
    });

    it('should add or update Query Definition', () => {
        //Validate adding ResiliencyScore Metric Config
        spyOn(queryService, 'addQuery').and.returnValue(of(queryDefaultValue));
        component.addOrUpdateQuery(queryDefaultValue, 'Add');
        expect(component.alertMessage).toBeTruthy();
        expect(queryService.addQuery).toHaveBeenCalled();
        expect(queryService.getAllQueries).toHaveBeenCalled();
    });

    it('should edit orupdate Query Definition', () => {
        //Validate editing ResiliencyScore Metric Config
        spyOn(queryService, 'updateQuery').and.returnValue(of(queryDefaultValue));
        component.addOrUpdateQuery(queryDefaultValue, 'Edit');
        expect(component.alertMessage).toBeTruthy();
        //expect(component.allQueries[0].name).toBe('defaultQuery');
        expect(queryService.updateQuery).toHaveBeenCalled();
        expect(queryService.getAllQueries).toHaveBeenCalled();
    });

    it('should delete Query', () => {
        spyOn(queryService, 'deleteQuery').and.returnValue(of({}));
        spyOn(window, 'confirm').and.callFake(function () { return true; });
        component.deleteQuery(queryDefaultValue.name);
        expect(component.alertMessage).toBeTruthy();
        expect(queryService.deleteQuery).toHaveBeenCalled();
    });

});
