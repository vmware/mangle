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
import { CoreService } from 'src/app/core/core.service';
import { CoreComponent } from 'src/app/core/core.component';
import { EventsComponent } from './events.component';
import { SharedService } from '../shared.service';

describe('EventsComponent', () => {
  let component: EventsComponent;
  let sharedService: SharedService;
  let coreService: CoreService;
  let fixture: ComponentFixture<EventsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'events', component: EventsComponent }])
      ],
      declarations: [EventsComponent, CoreComponent],
      providers: [
        SharedService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(EventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    sharedService = TestBed.get(SharedService);
    spyOn(sharedService, 'getAppEvents').and.returnValue(of({}));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get events', () => {
    component.getAppEvents();
    expect(sharedService.getAppEvents).toHaveBeenCalled();
  });

});
