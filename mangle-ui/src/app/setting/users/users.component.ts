import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-users',
    templateUrl: './users.component.html',
    styleUrls: ['./users.component.css']
})
export class UsersComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public userFormData;
    public passwordFieldRequired: boolean = false;
    public currentSelectedRoles: any = [];

    public authSources: any = [];
    public userList: any;
    public roleList: any;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getUserList();
        this.getRoleList();
        this.getDomains();
    }

    public populateAddUserForm(addUserData) {
        this.getRoleList();
        this.userFormData = addUserData;
        this.currentSelectedRoles = [];
    }

    public populateEditUserForm(editUserData) {
        this.getRoleList();
        this.userFormData = editUserData;
        this.passwordFieldRequired = false;
        var userAndDomain = editUserData.name.split("@");
        if (userAndDomain[1] === "mangle.local") {
            this.passwordFieldRequired = true;
            this.userFormData.password = null;
        }
        this.currentSelectedRoles = [];
        for (var i = 0; i < editUserData.roleNames.length; i++) {
            this.currentSelectedRoles.push(editUserData.roleNames[i]);
        }
    }

    public updateCurrentSelectedRoles(roleEvent, role) {
        if (roleEvent.target.checked) {
            this.currentSelectedRoles.push(role.name);
        } else {
            for (var i = 0; i < this.currentSelectedRoles.length; i++) {
                if (this.currentSelectedRoles[i] == role.name) {
                    this.currentSelectedRoles.splice(i, 1);
                }
            }
        }
    }

    public getUserList() {
        this.isLoading = true;
        this.settingService.getUserList().subscribe(
            res => {
                this.userList = res._embedded.userList;
                this.isLoading = false;
            }, err => {
                this.userList = [];
                this.isLoading = false;
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            });
    }

    public getRoleList() {
        this.settingService.getRoleList().subscribe(
            res => {
                this.roleList = res._embedded.roleList;
            });
    }

    public addUser(addUserFormValue) {
        addUserFormValue.roleNames = this.currentSelectedRoles;
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        addUserFormValue.name = addUserFormValue.name + "@" + addUserFormValue.authSource;
        delete addUserFormValue["authSource"];
        this.settingService.addUser(addUserFormValue).subscribe(
            res => {
                this.getUserList();
                this.alertMessage = addUserFormValue.name + MessageConstants.USER_ADD;
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getUserList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateUser(updateUserFormValue) {
        updateUserFormValue.roleNames = this.currentSelectedRoles;
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.updateUser(updateUserFormValue).subscribe(
            res => {
                this.ngOnInit();
                this.alertMessage = updateUserFormValue.name + MessageConstants.USER_UPDATE;
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.ngOnInit();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteUser(user) {
        this.errorFlag = false;
        this.successFlag = false;
        if (confirm(MessageConstants.DELETE_CONFIRM + user.name + MessageConstants.QUESTION_MARK)) {
            this.settingService.deleteUser(user.name).subscribe(
                res => {
                    this.getUserList();
                    this.alertMessage = user.name + MessageConstants.USER_DELETE;
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getUserList();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
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
                if (res._embedded != null) {
                    for (var i = 0; i < res._embedded.stringList.length; i++) {
                        this.authSources[i] = res._embedded.stringList[i];
                    }
                } else {
                    this.authSources = [];
                }
            });
    }

}
