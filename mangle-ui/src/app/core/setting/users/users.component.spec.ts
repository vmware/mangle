import {ComponentFixture, TestBed} from "@angular/core/testing";

import {UsersComponent} from "./users.component";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {FormsModule} from "@angular/forms";
import {RouterTestingModule} from "@angular/router/testing";
import {SettingService} from "../setting.service";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {BrowserModule} from "@angular/platform-browser";
import {CommonModule} from "@angular/common";
import {ClarityModule} from "@clr/angular";
import {of} from "rxjs";
import {CoreComponent} from "src/app/core/core.component";
import {CoreService} from "src/app/core/core.service";
import {delay} from "rxjs/operators";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe("UsersComponent", () => {
  let component: UsersComponent;
  let settingService: SettingService;
  let coreService: CoreService;
  let fixture: ComponentFixture<UsersComponent>;

  const user_data = {"name": "user@mangle.local", "password": "password", "roleNames": ["ROLE_USER"]};
  const role_data = {"name": "ROLE_USER", "privilegeNames": ["READONLY"]};

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [
        BrowserAnimationsModule,
        BrowserModule,
        FormsModule,
        HttpClientTestingModule,
        CommonModule,
        ClarityModule,
        RouterTestingModule.withRoutes([{path: "users", component: UsersComponent}])
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
    spyOn(settingService, "getUserList").and.returnValue(of({"content": [user_data]}));
    spyOn(settingService, "getRoleList").and.returnValue(of({"content": [role_data]}));
    spyOn(settingService, "getDomains").and.returnValue(of({"content": ["mangle.local"]}));
    coreService = TestBed.get(CoreService);
    spyOn(coreService, "getMyDetails").and.returnValue(of({"name": "user@mangle.local"}));
  });

  it("should create", () => {
    expect(component).toBeTruthy();
  });

  it("should get user list", () => {
    component.getUserList();
    expect(component.userList[0].name).toBe("user@mangle.local");
    expect(settingService.getUserList).toHaveBeenCalled();
  });

  it("should get role list", () => {
    component.getRoleList();
    expect(component.roleList[0].name).toBe("ROLE_USER");
    expect(settingService.getRoleList).toHaveBeenCalled();
  });

  it("should add role", () => {
    component.currentSelectedRoles = ["ROLE_READONLY"];
    component.userFormData = user_data;
    // add role
    spyOn(settingService, "addUser").and.returnValue(of(user_data));
    component.addUser(user_data);
    expect(component.alertMessage).toBeTruthy();
    expect(settingService.addUser).toHaveBeenCalled();
    expect(settingService.getUserList).toHaveBeenCalled();
  });

  it("should update role", () => {
    component.currentSelectedRoles = ["ROLE_READONLY"];
    component.userFormData = user_data;
    // update role
    spyOn(settingService, "updateUser").and.returnValue(of(user_data));
    component.updateUser(user_data);
    delay(20);
    expect(settingService.updateUser).toHaveBeenCalledTimes(1);
    expect(settingService.getUserList).toHaveBeenCalledTimes(1);
  });


  it("should delete user", () => {
    spyOn(settingService, "deleteUser").and.returnValue(of({}));
    spyOn(window, "confirm").and.callFake(function () {
      return true;
    });
    component.deleteUser(user_data);
    expect(component.alertMessage).toBeTruthy();
    expect(settingService.deleteUser).toHaveBeenCalled();
    expect(settingService.getUserList).toHaveBeenCalled();
  });

});
