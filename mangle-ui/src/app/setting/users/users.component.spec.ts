import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UsersComponent } from './users.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { RouterTestingModule } from '@angular/router/testing';
import { SettingService } from '../setting.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { ClarityModule } from '@clr/angular';
import { of } from 'rxjs';
import { CoreComponent } from 'src/app/core/core.component';
import { CoreService } from 'src/app/core/core.service';

describe('UsersComponent', () => {
  let component: UsersComponent;
  let settingService: SettingService;
  let coreService: CoreService;
  let fixture: ComponentFixture<UsersComponent>;

  let user_data = { "name": "user@mangle.local", "password": "password", "roleNames": ["ROLE_USER"] };
  let role_data = { "name": "ROLE_USER", "privilegeNames": ["READONLY"] };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'users', component: UsersComponent }])
      ],
      declarations: [UsersComponent, CoreComponent],
      providers: [
        SettingService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    settingService = TestBed.get(SettingService);
    spyOn(settingService, 'getUserList').and.returnValue(of({ "_embedded": { "userList": [user_data] } }));
    spyOn(settingService, 'getRoleList').and.returnValue(of({ "_embedded": { "roleList": [role_data] } }));
    spyOn(settingService, 'getDomains').and.returnValue(of({ "_embedded": { "stringList": ["mangle.local"] } }));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get user list', () => {
    component.getUserList();
    expect(component.userList[0].name).toBe("user@mangle.local");
    expect(settingService.getUserList).toHaveBeenCalled();
  });

  it('should get role list', () => {
    component.getRoleList();
    expect(component.roleList[0].name).toBe("ROLE_USER");
    expect(settingService.getRoleList).toHaveBeenCalled();
  });

  it('should add or update role', () => {
    component.currentSelectedRoles = ["ROLE_USER"];
    component.userFormData = user_data;
    //add role
    spyOn(settingService, 'addUser').and.returnValue(of(user_data));
    component.addUser(user_data);
    expect(component.successFlag).toBe(true);
    expect(settingService.addUser).toHaveBeenCalled();
    expect(settingService.getUserList).toHaveBeenCalled();
    //update role
    spyOn(settingService, 'updateUser').and.returnValue(of(user_data));
    component.updateUser(user_data);
    expect(component.successFlag).toBe(true);
    expect(settingService.updateUser).toHaveBeenCalled();
    expect(settingService.getUserList).toHaveBeenCalled();
  });

  it('should delete user', () => {
    spyOn(settingService, 'deleteUser').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteUser(user_data);
    expect(component.successFlag).toBe(true);
    expect(settingService.deleteUser).toHaveBeenCalled();
    expect(settingService.getUserList).toHaveBeenCalled();
  });

});
