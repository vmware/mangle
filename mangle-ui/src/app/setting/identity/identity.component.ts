import { Component, OnInit } from '@angular/core';
import { SettingService } from '../setting.service';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-identity',
    templateUrl: './identity.component.html'
})
export class IdentityComponent implements OnInit {

    constructor(private settingService: SettingService) { }

    public identities: any;
    public identityFormData: any;

    public errorAlertMessage: string;
    public successAlertMessage: string;

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
            }, err => {
                this.identities = [];
                this.isLoading = false;
                this.errorAlertMessage = err.error.description;
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
        this.isLoading = true;
        this.settingService.addIdentitySource(identitySourceFormData).subscribe(
            res => {
                this.getIdentities();
                this.successAlertMessage = identitySourceFormData.adDomain + MessageConstants.IDENTITY_ADD;
                this.isLoading = false;
            }, err => {
                this.errorAlertMessage = err.error.description;
                this.getIdentities();
                this.isLoading = false;
            });
    }

    public updateIdentitySource(identitySourceFormData) {
        this.isLoading = true;
        this.settingService.updateIdentitySource(identitySourceFormData).subscribe(
            res => {
                this.getIdentities();
                this.successAlertMessage = identitySourceFormData.adDomain + MessageConstants.IDENTITY_UPDATE;
                this.isLoading = false;
            }, err => {
                this.getIdentities();
                this.errorAlertMessage = err.error.description;
                this.isLoading = false;
            });
    }

    public deleteIdentity(identity) {
        if (confirm(MessageConstants.DELETE_CONFIRM + identity.adDomain + MessageConstants.QUESTION_MARK)) {
            this.settingService.deleteIdentity(identity.adDomain).subscribe(
                res => {
                    this.getIdentities();
                    this.successAlertMessage = identity.adDomain + MessageConstants.IDENTITY_DELETE;
                    this.isLoading = false;
                }, err => {
                    this.getIdentities();
                    this.errorAlertMessage = err.error.description;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

}
