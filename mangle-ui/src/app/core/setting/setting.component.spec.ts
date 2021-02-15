import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SettingComponent } from './setting.component';
import { RouterTestingModule } from '@angular/router/testing';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { CoreService } from '../core.service';
import { CoreComponent } from '../core.component';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('SettingComponent', () => {
  let component: SettingComponent;
  let coreService: CoreService;
  let fixture: ComponentFixture<SettingComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        CommonModule,
        HttpClientTestingModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'setting', component: SettingComponent }])
      ],
      declarations: [SettingComponent, CoreComponent],
      providers: [
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(SettingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
