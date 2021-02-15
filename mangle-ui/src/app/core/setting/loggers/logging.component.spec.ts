import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoggingComponent } from './logging.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { SettingService } from '../setting.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { CoreService } from 'src/app/core/core.service';
import { CoreComponent } from 'src/app/core/core.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('LoggingComponent', () => {
  let component: LoggingComponent;
  let settingService: SettingService;
  let coreService: CoreService;
  let fixture: ComponentFixture<LoggingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientTestingModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'logging', component: LoggingComponent }])
      ],
      declarations: [LoggingComponent, CoreComponent],
      providers: [
        SettingService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    settingService = TestBed.get(SettingService);
    spyOn(settingService, 'getLoggers').and.returnValue(of({}));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
    fixture = TestBed.createComponent(LoggingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
