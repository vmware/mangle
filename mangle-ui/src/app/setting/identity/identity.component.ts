import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';

@Component({
    selector: 'app-identity',
    templateUrl: './identity.component.html',
    styleUrls: ['./identity.component.css']
})
export class IdentityComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public identities: any;
    public identityFormData: any;

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getIdentities();
    }

    public populateIdentityForm(identityData: any) {
        this.identityFormData = identityData;
    }

    public getIdentities() {
        this.isLoading = true;
        this.settingService.getIdentities().subscribe(
            res => {
                if (res._embedded != null) {
                    this.identities = res._embedded.aDAuthProviderDtoList;
                    this.isLoading = false;
                } else {
                    this.identities = [];
                    this.isLoading = false;
                }
            });
    }

    public addOrUpdateIdentitySource(identitySourceFormData) {
        if (identitySourceFormData.id == null) {
            this.addIdentitySource(identitySourceFormData);
        } else {
            this.updateIdentitySource(identitySourceFormData);
        }
    }

    public addIdentitySource(identitySourceFormData) {
        delete identitySourceFormData["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.addIdentitySource(identitySourceFormData).subscribe(
            res => {
                this.getIdentities();
                this.alertMessage = 'Identity source added successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.alertMessage = err.error.description;
                this.getIdentities();
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateIdentitySource(identitySourceFormData) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.settingService.updateIdentitySource(identitySourceFormData).subscribe(
            res => {
                this.getIdentities();
                this.alertMessage = 'Identity source updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getIdentities();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteIdentity(identity) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        if (confirm('Do you want to delete: ' + identity.adDomain + ' identity source?')) {
            this.settingService.deleteIdentity(identity.adDomain).subscribe(
                res => {
                    this.getIdentities();
                    this.alertMessage = identity.adUrl + ' identity source deleted successfully!';
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getIdentities();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

}
