import { Component, OnInit } from '@angular/core';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { NgForm } from '@angular/forms';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-endpoint-credentials',
    templateUrl: './endpoint-credentials.component.html',
    styleUrls: ['./endpoint-credentials.component.css']
})
export class EndpointCredentialsComponent implements OnInit {

    constructor(private endpointService: EndpointService) { }

    public credentials: any;
    public credentialFormData: any;
    public k8sFileToUpload: any;
    public keyFileToUpload: any;

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

    public addEdit: string;

    public authErrorFlag = false;
    public authAlertMessage: string;

    public passwordHidden: boolean = true;
    public privateKeyHidden: boolean = true;

    public machineCredential: boolean = false;

    public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getCredentials();
    }

    public populateCredentialForm(credentialData: any) {
        this.authErrorFlag = false;
        this.credentialFormData = credentialData;
    }

    public showAuthorization(selectedResource) {
        this.passwordHidden = true;
        this.privateKeyHidden = true;
        if (selectedResource == "apassword") {
            this.passwordHidden = false;
        }
        if (selectedResource == "privateKey") {
            this.privateKeyHidden = false;
        }
    }

    public getCredentials() {
        this.isLoading = true;
        this.errorFlag = false;
        this.endpointService.getCredentials().subscribe(
            res => {
                if (res.code) {
                    this.credentials = [];
                    this.isLoading = false;
                } else {
                    this.credentials = res;
                    this.isLoading = false;
                }
            }, err => {
                this.credentials = [];
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public getPvtKeyFiles(fileToUploadEvent) {
        this.keyFileToUpload = fileToUploadEvent.target.files[0];
    }

    public addUpdateMachineCredential(credentialForm: NgForm) {
        if (credentialForm.value.id == null) {
            this.addMachineCredential(credentialForm);
        } else {
            this.updateMachineCredential(credentialForm);
        }
    }

    public addMachineCredential(credentialForm: NgForm) {
        var credential = credentialForm.value;
        this.authErrorFlag = false;
        this.submitBtnState = ClrLoadingState.LOADING;
        delete credential["id"];
        this.errorFlag = false;
        this.successFlag = false;
        if (typeof this.keyFileToUpload != undefined || (typeof credential.password != undefined && credential.password != "" && credential.password != null)) {
            this.endpointService.addRemoteMachineCredential(credential, this.keyFileToUpload).subscribe(
                res => {
                    this.getCredentials();
                    this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                    this.successFlag = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    credentialForm.reset();
                    this.machineCredential = false;
                }, err => {
                    this.getCredentials();
                    this.alertMessage = err.error.description;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                    this.errorFlag = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    credentialForm.reset();
                    this.machineCredential = false;
                });
        } else {
            this.authAlertMessage = MessageConstants.PASSWORD_OR_KEY_REQUIRED;
            this.authErrorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
            return;
        }
    }

    public updateMachineCredential(credentialForm: NgForm) {
        var credential = credentialForm.value;
        this.authErrorFlag = false;
        this.submitBtnState = ClrLoadingState.LOADING;
        this.errorFlag = false;
        this.successFlag = false;
        if (typeof this.keyFileToUpload != undefined || (typeof credential.password != undefined && credential.password != "" && credential.password != null)) {
            this.endpointService.updateRemoteMachineCredential(credential, this.keyFileToUpload).subscribe(
                res => {
                    this.getCredentials();
                    this.alertMessage = credential.name + MessageConstants.CREDENTIAL_UPDATE;
                    this.successFlag = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    this.machineCredential = false;
                    credentialForm.reset();
                }, err => {
                    this.getCredentials();
                    this.alertMessage = err.error.description;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                    this.errorFlag = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    this.machineCredential = false;
                    credentialForm.reset();
                });
        } else {
            this.authAlertMessage = MessageConstants.PASSWORD_OR_KEY_REQUIRED;
            this.authErrorFlag = true;
            this.submitBtnState = ClrLoadingState.DEFAULT;
            return;
        }
    }

    public getK8SFiles(fileToUploadEvent) {
        this.k8sFileToUpload = fileToUploadEvent.target.files[0];
    }

    public addUpdateKubernetesCredential(credential) {
        if (credential.id == null) {
            this.addKubernetesCredential(credential);
        } else {
            this.updateKubernetesCredential(credential);
        }
    }

    public addKubernetesCredential(credential) {
        this.isLoading = true;
        delete credential["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.endpointService.addk8sCredential(credential, this.k8sFileToUpload).subscribe(
            res => {
                this.getCredentials();
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateKubernetesCredential(credential) {
        this.isLoading = true;
        this.errorFlag = false;
        this.successFlag = false;
        this.endpointService.updatek8sCredential(credential, this.k8sFileToUpload).subscribe(
            res => {
                this.getCredentials();
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_UPDATE;
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public addUpdateVcenterCredential(credential) {
        if (credential.id == null) {
            this.addVcenterCredential(credential);
        } else {
            this.updateVcenterCredential(credential);
        }
    }

    public addVcenterCredential(credential) {
        this.isLoading = true;
        this.errorFlag = false;
        this.successFlag = false;
        delete credential["id"];
        this.endpointService.addVcenterCredential(credential).subscribe(
            res => {
                this.getCredentials();
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateVcenterCredential(credential) {
        this.isLoading = true;
        this.errorFlag = false;
        this.successFlag = false;
        delete credential["type"];
        this.endpointService.updateVcenterCredential(credential).subscribe(
            res => {
                this.getCredentials();
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_UPDATE;
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public deleteCredential(credential) {
        this.errorFlag = false;
        this.successFlag = false;
        if (confirm(MessageConstants.DELETE_CONFIRM + credential.name + MessageConstants.QUESTION_MARK)) {
            this.isLoading = true;
            this.endpointService.deleteCredential(credential.name).subscribe(
                res => {
                    this.getCredentials();
                    this.alertMessage = credential.name + MessageConstants.CREDENTIAL_DELETE;
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getCredentials();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.isLoading = false;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                });
        } else {
            // Do nothing!
        }
    }

}
