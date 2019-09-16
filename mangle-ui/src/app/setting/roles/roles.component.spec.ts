import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RolesComponent } from './roles.component';
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
import { CoreService } from 'src/app/core/core.service';
import { CoreComponent } from 'src/app/core/core.component';

describe('RolesComponent', () => {
  let component: RolesComponent;
  let settingService: SettingService;
  let coreService: CoreService;
  let fixture: ComponentFixture<RolesComponent>;

  let role_data: any = { "id": null, "name": "CUSTOM_ROLE", "privilegeNames": ["USER_READ_WRITE"] };
  let role_data_id: any = { "id": "with_id", "name": "CUSTOM_ROLE", "privilegeNames": ["USER_READ_WRITE"] };
  let privilege_data: any = { "name": "USER_READ_WRITE" };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{ path: 'roles', component: RolesComponent }])
      ],
      declarations: [RolesComponent, CoreComponent],
      providers: [
        SettingService,
        CoreService
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
      .compileComponents();
    fixture = TestBed.createComponent(RolesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    settingService = TestBed.get(SettingService);
    spyOn(settingService, 'getRoleList').and.returnValue(of({ "_embedded": { "roleList": [role_data] } }));
    spyOn(settingService, 'getPrivilegeList').and.returnValue(of({ "_embedded": { "privilegeList": [privilege_data] } }));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, 'getMyDetails').and.returnValue(of({ "name": "user@mangle.local" }));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should get privilege list', () => {
    component.getPrivilegeList();
    expect(component.privilegeList[0].name).toBe("USER_READ_WRITE");
    expect(settingService.getPrivilegeList).toHaveBeenCalled();
  });

  it('should get role list', () => {
    component.getRoleList();
    expect(component.roleList[0].name).toBe("CUSTOM_ROLE");
    expect(settingService.getRoleList).toHaveBeenCalled();
  });

  it('should add or update role', () => {
    component.currentSelectedPrivileges = ["USER_READ_WRITE"];
    component.roleFormData = role_data;
    //add role
    spyOn(settingService, 'addRole').and.returnValue(of(role_data_id));
    component.addOrUpdateRole(role_data);
    expect(component.successAlertMessage).toBeTruthy();
    expect(settingService.addRole).toHaveBeenCalled();
    expect(settingService.getRoleList).toHaveBeenCalled();
    //update role
    spyOn(settingService, 'updateRole').and.returnValue(of(role_data_id));
    component.addOrUpdateRole(role_data_id);
    expect(component.successAlertMessage).toBeTruthy();
    expect(settingService.updateRole).toHaveBeenCalled();
    expect(settingService.getRoleList).toHaveBeenCalled();
  });

  it('should delete role', () => {
    spyOn(settingService, 'deleteRole').and.returnValue(of({}));
    spyOn(window, 'confirm').and.callFake(function () { return true; });
    component.deleteRole(role_data);
    expect(component.successAlertMessage).toBeTruthy();
    expect(settingService.deleteRole).toHaveBeenCalled();
    expect(settingService.getRoleList).toHaveBeenCalled();
  });

});
