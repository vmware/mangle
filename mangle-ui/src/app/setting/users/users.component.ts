import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { AuthService } from 'src/app/auth/auth.service';

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
    public authSources: any = [];
    public userList: any;
    public roleList: any;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getUserList();
        this.getRoleList();
        this.getIdentities();
    }

    public populateUserForm(userData: any) {
        if (userData.name != null) {
            userData.roleNames = [];
            userData.name = userData.name + "@" + userData.authSource;
            delete userData["authSource"];
        }
        this.userFormData = userData;
    }

    public updateUserFormData(roleEvent, role) {
        if (roleEvent.target.checked) {
            this.userFormData.roleNames.push(role.name);
        } else {
            for (var i = 0; i < this.userFormData.roleNames.length; i++) {
                if (this.userFormData.roleNames[i] == role.name) {
                    this.userFormData.roleNames.splice(i, 1);
                }
            }
        }
    }

    public getUserList() {
        this.isLoading = true;
        this.settingService.getUserList().subscribe(
            res => {
                this.userList = res._embedded.userList;
                for (var i = 0; i < this.userList.length; i++) {
                    var userSplit = this.userList[i].name.split("@");
                    this.userList[i].name = userSplit[0];
                    this.userList[i].authSource = userSplit[1];
                }
                this.isLoading = false;
            });
    }

    public getRoleList() {
        this.settingService.getRoleList().subscribe(
            res => {
                this.roleList = res._embedded.roleList;
            });
    }

    public addOrUpdateUser(userFormValue) {
        userFormValue.roleNames = this.userFormData.roleNames;
        if (userFormValue.id == null) {
            userFormValue.name = userFormValue.name + "@" + userFormValue.authSource;
            delete userFormValue["authSource"];
            this.addUser(userFormValue);
        } else {
            this.updateUser(userFormValue);
        }
        this.getRoleList();
    }

    public addUser(userFormValue) {
        delete userFormValue["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.addUser(userFormValue).subscribe(
            res => {
                this.getUserList();
                this.alertMessage = 'User added successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getUserList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateUser(userFormValue) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.updateUser(userFormValue).subscribe(
            res => {
                this.getUserList();
                this.alertMessage = 'User updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getUserList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteUser(user) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        if (confirm('Do you want to delete: ' + user.name + '@' + user.authSource + ' user ?')) {
            this.settingService.deleteUser(user.name + '@' + user.authSource).subscribe(
                res => {
                    this.getUserList();
                    this.alertMessage = user.name + '@' + user.authSource + ' user deleted successfully!';
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

    public getIdentities() {
        this.isLoading = true;
        this.settingService.getIdentities().subscribe(
            res => {
                if (res._embedded != null) {
                    for (var i = 0; i < res._embedded.aDAuthProviderDtoList.length; i++) {
                        this.authSources[i] = res._embedded.aDAuthProviderDtoList[i].adDomain;
                    }
                } else {
                    this.authSources = [];
                }
            });
    }

}
