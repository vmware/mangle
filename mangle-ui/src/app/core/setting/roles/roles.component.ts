import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { ClrLoadingState } from '@clr/angular';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-roles',
    templateUrl: './roles.component.html'
})
export class RolesComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public alertMessage: string;
    public isErrorMessage: boolean;

    public roleModal: boolean = false;

    public privilegeErrorFlag = false;
    public privilegeAlertMessage: string;

    public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public addEdit: string;

    public roleFormData;
    public roleList: any;
    public privilegeList: any;

    public currentSelectedPrivileges: any = [];

    public isLoading: boolean = true;

    ngOnInit() {
        this.getRoleList();
        this.getPrivilegeList();
    }

    public populateRoleForm(roleData: any) {
        this.roleFormData = roleData;
        this.currentSelectedPrivileges = [];
        for (var i = 0; i < roleData.privilegeNames.length; i++) {
            this.currentSelectedPrivileges.push(roleData.privilegeNames[i]);
        }
    }

    public updateRoleFormData(privilegeEvent, privilege) {
        if (privilegeEvent.target.checked) {
            this.currentSelectedPrivileges.push(privilege.name);
        } else {
            for (var i = 0; i < this.currentSelectedPrivileges.length; i++) {
                if (this.currentSelectedPrivileges[i] == privilege.name) {
                    this.currentSelectedPrivileges.splice(i, 1);
                }
            }
        }
    }

    public getRoleList() {
        this.isLoading = true;
        this.settingService.getRoleList().subscribe(
            res => {
                this.roleList = res.content;
                this.isLoading = false;
            }, err => {
                this.roleList = [];
                this.isLoading = false;
                this.isErrorMessage= true;
                this.alertMessage = err.error.description;
            });
    }

    public getPrivilegeList() {
        this.settingService.getPrivilegeList().subscribe(
            res => {
                this.privilegeList = res.content;
            });
    }

    public addOrUpdateRole(roleFormValue) {
        this.submitBtnState = ClrLoadingState.LOADING;
        if (this.currentSelectedPrivileges.length != 0) {
            roleFormValue.privilegeNames = this.currentSelectedPrivileges;
            if (roleFormValue.id == null) {
                this.addRole(roleFormValue);
            } else {
                this.updateRole(roleFormValue);
            }
            this.getPrivilegeList();
        } else {
            this.privilegeAlertMessage = MessageConstants.MIN_PRIVILEGE_REQUIRED;
            this.privilegeErrorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
        }
    }

    public addRole(roleFormValue) {
        delete roleFormValue["id"];
        this.isLoading = true;
        this.settingService.addRole(roleFormValue).subscribe(
            res => {
                this.getRoleList();
                this.isErrorMessage = false;
                this.alertMessage = roleFormValue.name + MessageConstants.ROLE_ADD;
                this.isLoading = false;
                this.submitBtnState = ClrLoadingState.DEFAULT;
                this.roleModal = false;
            }, err => {
                this.getRoleList();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                this.submitBtnState = ClrLoadingState.DEFAULT;
                this.roleModal = false;
            });
    }

    public updateRole(roleFormValue) {
        this.isLoading = true;
        this.settingService.updateRole(roleFormValue).subscribe(
            res => {
                this.getRoleList();
                this.isErrorMessage= false;
                this.alertMessage = roleFormValue.name + MessageConstants.ROLE_UPDATE;
                this.isLoading = false;
                this.submitBtnState = ClrLoadingState.DEFAULT;
                this.roleModal = false;
            }, err => {
                this.getRoleList();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                this.submitBtnState = ClrLoadingState.DEFAULT;
                this.roleModal = false;
            });
    }

    public deleteRole(role) {
        if (confirm(MessageConstants.DELETE_CONFIRM + role.name + MessageConstants.QUESTION_MARK)) {
            this.settingService.deleteRole(role.name).subscribe(
                res => {
                    this.getRoleList();
                    this.isErrorMessage= false;
                    this.alertMessage = role.name + MessageConstants.ROLE_DELETE;
                    this.isLoading = false;
                }, err => {
                    this.getRoleList();
                    this.isErrorMessage= true;
                    this.alertMessage = err.error.description;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

}
