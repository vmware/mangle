import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { MessageConstants } from 'src/app/common/message.constants';

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
        this.errorFlag = false;
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
            }, err => {
                this.identities = [];
                this.isLoading = false;
                this.alertMessage = err.error.description;
                this.errorFlag = true;
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
                this.alertMessage = identitySourceFormData.adDomain + MessageConstants.IDENTITY_ADD;
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
                this.alertMessage = identitySourceFormData.adDomain + MessageConstants.IDENTITY_UPDATE;
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
        if (confirm(MessageConstants.DELETE_CONFIRM + identity.adDomain + MessageConstants.QUESTION_MARK)) {
            this.settingService.deleteIdentity(identity.adDomain).subscribe(
                res => {
                    this.getIdentities();
                    this.alertMessage = identity.adDomain + MessageConstants.IDENTITY_DELETE;
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
