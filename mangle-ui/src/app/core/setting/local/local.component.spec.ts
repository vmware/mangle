import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LocalComponent } from './local.component';
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

describe('LocalComponent', () => {
  let component: LocalComponent;
  let settingService: SettingService;
  let coreService: CoreService;
  let fixture: ComponentFixture<LocalComponent>;

  let local_user: any = { "id": null, "username": "user@mangle.local", "password": null };
  let local_user_id: any = { "id": "with_id", "username": "user@mangle.local" };

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientTestingModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'local', component: LocalComponent }])
      ],
      declarations: [LocalComponent, CoreComponent],
      providers: [
        SettingService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    settingService = TestBed.get(SettingService);
    spyOn(settingService, 'getLocalUserList').and.returnValue(of({ "content": [local_user_id] }));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
    fixture = TestBed.createComponent(LocalComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should populate local user form', () => {
    component.populateLocalUserForm(local_user);
    expect(component.localUserFormData.username).toBe("user@mangle.local");
  });

  it('should get local user list', () => {
    component.getLocalUserList();
    expect(component.localUserList[0].username).toBe("user@mangle.local");
    expect(settingService.getLocalUserList).toHaveBeenCalled();
  });

  it('should add or update local user', () => {
    //add user
    spyOn(settingService, 'addLocalUser').and.returnValue(of(local_user_id));
    component.addOrUpdateLocalUser(local_user);
    expect(component.alertMessage).toBeTruthy();
    expect(settingService.addLocalUser).toHaveBeenCalled();
    expect(settingService.getLocalUserList).toHaveBeenCalled();
    //update user
    spyOn(settingService, 'updateLocalUser').and.returnValue(of(local_user_id));
    component.addOrUpdateLocalUser(local_user_id);
    expect(component.alertMessage).toBeTruthy();
    expect(settingService.updateLocalUser).toHaveBeenCalled();
    expect(settingService.getLocalUserList).toHaveBeenCalled();
  });

  it('should delete local user', () => {
    spyOn(settingService, 'deleteLocalUser').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteLocalUser(local_user);
    expect(component.alertMessage).toBeTruthy();
    expect(settingService.deleteLocalUser).toHaveBeenCalled();
    expect(settingService.getLocalUserList).toHaveBeenCalled();
  });

});
