import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotifierComponent } from './notifier.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { NotifierService } from './notifier.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('NotificationComponent', () => {
  let component: NotifierComponent;
  let slackService: NotifierService;
  let fixture: ComponentFixture<NotifierComponent>;

  let slack_data: any = {
    'name': 'test', 'notifierType': 'SLACK', 'slackInfo': { 'token': '1234', 'channels': ['dev'], 'senderName': 'Mangle' },
    'enable': true
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
        RouterTestingModule.withRoutes([{ path: 'notifier', component: NotifierComponent }])
      ],
      declarations: [NotifierComponent],
      providers: [
        NotifierService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    slackService = TestBed.get(NotifierService);
    spyOn(slackService, 'getNotificationInfo').and.returnValue(of([slack_data]));
    fixture = TestBed.createComponent(NotifierComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create Slack Component', () => {
    expect(component).toBeTruthy();
  });

  it('should populate slack form', () => {
    component.populateSlackForm({});
    expect(component.disableSubmit).toBe(true);
  });

  it('should get slacks', () => {
    component.getNotificationInfo();
    expect(component.notifications[0].name).toBe('test');
    expect(slackService.getNotificationInfo).toHaveBeenCalled();
  });

  it('should add or update slack', () => {
    //add slack
    spyOn(slackService, 'addNotification').and.returnValue(of(slack_data));
    component.addOrUpdateNotification(slack_data, 'Add');
    expect(component.alertMessage).toBeTruthy();
    expect(component.notifications[0].name).toBe('test');
    expect(slackService.addNotification).toHaveBeenCalled();
    expect(slackService.getNotificationInfo).toHaveBeenCalled();
    //update slack
    spyOn(slackService, 'updateNotification').and.returnValue(of(slack_data));
    component.addOrUpdateNotification(slack_data, 'Edit');
    expect(component.alertMessage).toBeTruthy();
    expect(component.notifications[0].name).toBe('test');
    expect(slackService.updateNotification).toHaveBeenCalled();
    expect(slackService.getNotificationInfo).toHaveBeenCalled();
  });

  it('should delete slack', () => {
    spyOn(slackService, 'deleteNotification').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteSlack(slack_data.name);
    expect(component.alertMessage).toBeTruthy();
    expect(slackService.deleteNotification).toHaveBeenCalled();
  });

  it('should test slack connection', () => {
    spyOn(slackService, 'testNotificationConnection').and.returnValue(of(slack_data));
    component.testNotificationConnection(true, slack_data);
    expect(component.testAlertMessage).toBeTruthy();
    expect(component.disableSubmit).toBe(false);
    expect(slackService.testNotificationConnection).toHaveBeenCalled();
  });

  it('should enable slack', () => {
    spyOn(slackService, 'enableNotification').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.enableSlacks(slack_data, true);
    expect(component.alertMessage).toBeTruthy();
    expect(slackService.enableNotification).toHaveBeenCalled();
    expect(slackService.getNotificationInfo).toHaveBeenCalled();
  });

  it('should disable slack', () => {
    spyOn(slackService, 'enableNotification').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.enableSlacks(slack_data, false);
    expect(component.alertMessage).toBeTruthy();
    expect(slackService.enableNotification).toHaveBeenCalled();
    expect(slackService.getNotificationInfo).toHaveBeenCalled();
  });

});
