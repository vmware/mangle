import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';

@Component({
    selector: 'app-local',
    templateUrl: './local.component.html',
    styleUrls: ['./local.component.css']
})
export class LocalComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public addEdit: string;

    public localUserFormData;
    public localUserList: any;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getLocalUserList();
    }

    public populateLocalUserForm(localUserData: any) {
        this.localUserFormData = localUserData;
    }

    public getLocalUserList() {
        this.isLoading = true;
        this.settingService.getLocalUserList().subscribe(
            res => {
                this.localUserList = res._embedded.userAuthenticationList;
                this.isLoading = false;
            });
    }

    public addOrUpdateLocalUser(localUserFormValue) {
        if (localUserFormValue.id == null) {
            localUserFormValue.username = localUserFormValue.username + "@mangle.local";
            this.addLocalUser(localUserFormValue);
        } else {
            this.updateLocalUser(localUserFormValue);
        }
    }

    public addLocalUser(localUserFormValue) {
        delete localUserFormValue["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.addLocalUser(localUserFormValue).subscribe(
            res => {
                this.getLocalUserList();
                this.alertMessage = 'User added successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getLocalUserList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateLocalUser(localUserFormValue) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.updateLocalUser(localUserFormValue).subscribe(
            res => {
                this.getLocalUserList();
                this.alertMessage = 'User updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getLocalUserList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteLocalUser(localUser) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        if (confirm('Do you want to delete: ' + localUser.username + ' user ?')) {
            this.settingService.deleteLocalUser(localUser.username).subscribe(
                res => {
                    this.getLocalUserList();
                    this.alertMessage = localUser.username + ' user deleted successfully!';
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getLocalUserList();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

}
