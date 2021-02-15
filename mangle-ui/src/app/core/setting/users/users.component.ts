import {Component, OnInit} from "@angular/core";
import {SettingService} from "../setting.service";
import {MessageConstants} from "src/app/common/message.constants";
import {CommonConstants} from "../../../common/common.constants";

@Component({
  selector: "app-users",
  templateUrl: "./users.component.html",
  styleUrls: ["./users.component.css"]
})
export class UsersComponent implements OnInit {

  constructor(private settingService: SettingService) {
  }

  public userFormData;
  public currentSelectedRoles: any = [];

  public alertMessage: string;
  public isErrorMessage: boolean;

  public authSources: any = [];
  public userList: any;
  public roleList: any;
  public authSourcesForUserAddition: any = [];

  public isLoading = true;
  public userModalAdd: boolean;
  public userModalEdit: boolean;

  public barLabel = "";
  public barColors = ["#DD2C00", "#FF6D00", "#FFD600", "#AEEA00", "#00C853"];
  public baseColor = "#DDD";
  public strengthLabels = ["(Useless)", "(Weak)", "(Normal)", "(Strong)", "(Great!)"];

  ngOnInit() {
    this.getUserList();
    this.getRoleList();
    this.getDomains();
    this.getDomainsForUserAddition();
  }

  public populateAddUserForm(addUserData) {
    this.getRoleList();
    this.userFormData = addUserData;
    this.currentSelectedRoles = [];
  }

  public populateEditUserForm(editUserData) {
    this.getRoleList();
    this.userFormData = editUserData;
    const userAndDomain = editUserData.name.split("@");
    if (userAndDomain[1] === CommonConstants.DEFAULT_DOMAIN) {
      this.userFormData.password = null;
    }
    this.currentSelectedRoles = [];
    for (let i = 0; i < editUserData.roleNames.length; i++) {
      this.currentSelectedRoles.push(editUserData.roleNames[i]);
    }
  }

  public updateCurrentSelectedRoles(roleEvent, role) {
    if (roleEvent.target.checked) {
      this.currentSelectedRoles.push(role.name);
    } else {
      for (let i = 0; i < this.currentSelectedRoles.length; i++) {
        if (this.currentSelectedRoles[i] === role.name) {
          this.currentSelectedRoles.splice(i, 1);
        }
      }
    }
  }


  public getUserList() {
    this.isLoading = true;
    this.settingService.getUserList().subscribe(
      res => {
        this.userList = res.content;
        this.isLoading = false;
      }, err => {
        this.userList = [];
        this.isLoading = false;
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public getRoleList() {
    this.settingService.getRoleList().subscribe(
      res => {
        this.roleList = res.content;
      });
  }

  public addUser(addUserFormValue) {
    addUserFormValue.roleNames = this.currentSelectedRoles;
    this.isLoading = true;
    addUserFormValue.name = addUserFormValue.name + "@" + addUserFormValue.authSource;
    delete addUserFormValue["authSource"];
    this.settingService.addUser(addUserFormValue).subscribe(
      res => {
        this.getUserList();
        this.isErrorMessage = false;
        this.alertMessage = addUserFormValue.name + MessageConstants.USER_ADD;
        this.isLoading = false;
      }, err => {
        this.getUserList();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public updateUser(updateUserFormValue) {
    updateUserFormValue.roleNames = this.currentSelectedRoles;
    this.isLoading = true;
    this.settingService.updateUser(updateUserFormValue).subscribe(
      res => {
        this.getUserList();
        this.isErrorMessage = false;
        this.alertMessage = updateUserFormValue.name + MessageConstants.USER_UPDATE;
        this.isLoading = false;
      }, err => {
        this.getUserList();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public deleteUser(user) {
    if (confirm(MessageConstants.DELETE_CONFIRM + user.name + MessageConstants.QUESTION_MARK)) {
      this.settingService.deleteUser(user.name).subscribe(
        res => {
          this.getUserList();
          this.isErrorMessage = false;
          this.alertMessage = user.name + MessageConstants.USER_DELETE;
          this.isLoading = false;
        }, err => {
          this.getUserList();
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
          this.isLoading = false;
        });
    } else {
      // Do nothing!
    }
  }


  public getDomains() {
    this.isLoading = true;
    this.settingService.getDomains().subscribe(
      res => {
        if (res.content != null) {
          for (let i = 0; i < res.content.length; i++) {
            this.authSources[i] = res.content[i];
          }
        } else {
          this.authSources = [];
        }
      });
  }

  public getDomainsForUserAddition() {
    this.isLoading = true;
    this.settingService.getIdentities().subscribe(res => {
      if (res.content) {
        this.authSourcesForUserAddition = res.content
          .filter(authProvider => (authProvider.adUser !== null)).map(authProvider => authProvider.adDomain);
        this.authSourcesForUserAddition.push(CommonConstants.DEFAULT_DOMAIN);
      }
    });
  }

  public updateUserAccountLockStatus(user) {
    this.settingService.updateUser(user).subscribe(
      res => {
        this.getUserList();
        this.isErrorMessage = false;
        this.alertMessage = MessageConstants.USER_UPDATE;
        this.isLoading = false;
      }, err => {
        this.getUserList();
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.isLoading = false;
      });
  }

  public isLocalUser(username: string) {
    return username.includes(CommonConstants.DEFAULT_DOMAIN);
  }
}
