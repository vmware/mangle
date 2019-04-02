import { async, ComponentFixture, TestBed } from '@angular/core/testing';

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

  let user_data: any = { "id": null, "name": "user", "authSource": "mangle.local", "roleNames": ["ROLE_USER"] };
  let user_data_id: any = { "id": "with_id", "name": "user", "authSource": "mangle.local", "roleNames": ["ROLE_USER"] };
  let user_list_data: any = { "id": "some_id", "name": "user@mangle.local", "roleNames": ["ROLE_USER"] };
  let role_list_data: any = { "id": "some_id", "name": "ROLE_USER", "privilegeNames": ["ADMIN_READ", "USER_READ_WRITE"] };

  beforeEach(async(() => {
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
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(UsersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    settingService = TestBed.get(SettingService);
    spyOn(settingService, 'getUserList').and.returnValue(of({ "_embedded": { "userList": [user_list_data] } }));
    spyOn(settingService, 'getRoleList').and.returnValue(of({ "_embedded": { "roleList": [role_list_data] } }));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get user list', () => {
    component.getUserList();
    expect(component.userList[0].name).toBe("user");
    expect(settingService.getUserList).toHaveBeenCalled();
  });

  it('should get role list', () => {
    component.getRoleList();
    expect(component.roleList[0].name).toBe("ROLE_USER");
    expect(settingService.getRoleList).toHaveBeenCalled();
  });

  it('should add or update role', () => {
    component.userFormData = user_data;
    //add role
    spyOn(settingService, 'addUser').and.returnValue(of(user_data_id));
    component.addOrUpdateUser(user_data);
    expect(component.successFlag).toBe(true);
    expect(settingService.addUser).toHaveBeenCalled();
    expect(settingService.getUserList).toHaveBeenCalled();
    //update role
    spyOn(settingService, 'updateUser').and.returnValue(of(user_data_id));
    component.addOrUpdateUser(user_data_id);
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
