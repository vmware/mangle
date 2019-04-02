import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';

@Component({
    selector: 'app-roles',
    templateUrl: './roles.component.html',
    styleUrls: ['./roles.component.css']
})
export class RolesComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public addEdit: string;

    public roleFormData;
    public roleList: any;
    public privilegeList: any;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getRoleList();
        this.getPrivilegeList();
    }

    public populateRoleForm(roleData: any) {
        roleData.privilegeNames = [];
        this.roleFormData = roleData;
    }

    public updateRoleFormData(privilegeEvent, privilege) {
        if (privilegeEvent.target.checked) {
            this.roleFormData.privilegeNames.push(privilege.name);
        } else {
            for (var i = 0; i < this.roleFormData.privilegeNames.length; i++) {
                if (this.roleFormData.privilegeNames[i] == privilege.name) {
                    this.roleFormData.privilegeNames.splice(i, 1);
                }
            }
        }
    }

    public getRoleList() {
        this.isLoading = true;
        this.settingService.getRoleList().subscribe(
            res => {
                this.roleList = res._embedded.roleList;
                this.isLoading = false;
            });
    }

    public getPrivilegeList() {
        this.settingService.getPrivilegeList().subscribe(
            res => {
                this.privilegeList = res._embedded.privilegeList;
            });
    }

    public addOrUpdateRole(roleFormValue) {
        roleFormValue.privilegeNames = this.roleFormData.privilegeNames;
        if (roleFormValue.id == null) {
            this.addRole(roleFormValue);
        } else {
            this.updateRole(roleFormValue);
        }
        this.getPrivilegeList();
    }

    public addRole(roleFormValue) {
        delete roleFormValue["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.addRole(roleFormValue).subscribe(
            res => {
                this.getRoleList();
                this.alertMessage = 'Role added successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getRoleList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateRole(roleFormValue) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.updateRole(roleFormValue).subscribe(
            res => {
                this.getRoleList();
                this.alertMessage = 'Role updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getRoleList();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteRole(role) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        if (confirm('Do you want to delete: ' + role.name + ' role ?')) {
            this.settingService.deleteRole(role.name).subscribe(
                res => {
                    this.getRoleList();
                    this.alertMessage = role.name + ' role deleted successfully!';
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getRoleList();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

}
