import { Component, EventEmitter, Input, Output } from '@angular/core';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { MessageConstants } from 'src/app/common/message.constants';
import { NgForm } from '@angular/forms';
import { ClrLoadingState } from '@clr/angular';
import { CommonConstants } from "src/app/common/common.constants";
@Component({
    selector: 'app-credential',
    templateUrl: './credential.component.html'
})
export class CredentialComponent {

    @Input() awsCredentialModal: boolean = false;
    @Input() awsCredentialFormData: any = { 'id': null, 'name': null, 'accessKey': null, 'secretKey': null };
    @Input() azureCredentialModal: boolean = false;
    @Input() azureCredentialFormData: any = { 'id': null, 'name': null, 'azureClientId': null, 'azureClientKey': null };
    @Input() vcenterCredentialModal: boolean = false;
    @Input() vcenterCredentialFormData: any = { 'id': null, 'name': null, 'userName': null, 'password': null };
    @Input() k8sCredentialModal: boolean = false;
    @Input() k8sCredentialFormData: any = { 'id': null, 'name': null };
    @Input() machineCredentialModal: boolean = false;
    @Input() machineCredentialFormData: any = { 'id': null, 'name': null, 'username': null, 'password': null, 'privateKey': null };
    @Input() databaseCredentialModal: boolean = false;
    @Input() databaseCredentialFormData: any = { 'name': null, 'dbType': 'POSTGRES', 'dbUserName': null, 'dbPassword': null, 'dbPort': 5432, 'dbName': null, 'dbSSLEnabled': false };

    @Input() addEdit: string = 'Add';
    @Output() outputMessage = new EventEmitter<any>();
    public outMessageData: any = { 'isErrorMessage': null, 'alertMessage': null, 'credentialData': null };

    public btnState: ClrLoadingState = ClrLoadingState.DEFAULT;
    public k8sFileToUpload: any;
    public keyFileToUpload: any;
    public authErrorFlag = false;
    public authAlertMessage: string;
    public passwordHidden: boolean = true;
    public privateKeyHidden: boolean = true;
    public supportedDatabaseTypes: any = [CommonConstants.POSTGRES, CommonConstants.MONGODB, CommonConstants.CASSANDRA];

    public constructor(private endpointService: EndpointService) {
    }

    public addUpdateCredential(credentialForm: NgForm) {
        this.btnState = ClrLoadingState.LOADING;
        if (credentialForm.value.id == null) {
            delete credentialForm.value["id"];
            if (this.awsCredentialModal) {
                this.addAwsCredential(credentialForm);
            } else if (this.azureCredentialModal) {
                this.addAzureCredential(credentialForm);
            } else if (this.vcenterCredentialModal) {
                this.addVcenterCredential(credentialForm);
            } else if (this.k8sCredentialModal) {
                this.addK8sCredential(credentialForm);
            } else if (this.machineCredentialModal) {
                this.addMachineCredential(credentialForm);
            } else if (this.databaseCredentialModal) {
                this.addDatabaseCredential(credentialForm);
            }
        } else {
            if (this.awsCredentialModal) {
                this.updateAwsCredential(credentialForm);
            } else if (this.azureCredentialModal) {
                this.updateAzureCredential(credentialForm);
            } else if (this.vcenterCredentialModal) {
                this.updateVcenterCredential(credentialForm);
            } else if (this.k8sCredentialModal) {
                this.updateK8sCredential(credentialForm);
            } else if (this.machineCredentialModal) {
                this.updateMachineCredential(credentialForm);
            } else if (this.databaseCredentialModal) {
                this.updateDatabaseCredential(credentialForm);
            }
        }
    }

    public addAwsCredential(credentialForm: NgForm) {
        this.endpointService.addAwsCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_ADD, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public updateAwsCredential(credentialForm: NgForm) {
        this.endpointService.updateAwsCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_UPDATE, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public addAzureCredential(credentialForm: NgForm) {
        this.endpointService.addAzureCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_ADD, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public updateAzureCredential(credentialForm: NgForm) {
        this.endpointService.updateAzureCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_UPDATE, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public addVcenterCredential(credentialForm: NgForm) {
        this.endpointService.addVcenterCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_ADD, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public updateVcenterCredential(credentialForm: NgForm) {
        this.endpointService.updateVcenterCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_UPDATE, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public getK8SFiles(fileToUploadEvent) {
        this.k8sFileToUpload = fileToUploadEvent.target.files[0];
    }

    public addK8sCredential(credentialForm: NgForm) {
        this.endpointService.addk8sCredential(credentialForm.value, this.k8sFileToUpload).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_ADD, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public updateK8sCredential(credentialForm: NgForm) {
        this.endpointService.updatek8sCredential(credentialForm.value, this.k8sFileToUpload).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_UPDATE, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public getPvtKeyFiles(fileToUploadEvent) {
        this.keyFileToUpload = fileToUploadEvent.target.files[0];
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

    public addMachineCredential(credentialForm: NgForm) {
        this.authErrorFlag = false;
        if (typeof this.keyFileToUpload != 'undefined' || (typeof credentialForm.value.password != 'undefined' && credentialForm.value.password != "" && credentialForm.value.password != null)) {
            this.endpointService.addRemoteMachineCredential(credentialForm.value, this.keyFileToUpload).subscribe(
                res => {
                    this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_ADD, credentialForm, null);
                }, err => {
                    this.finishAddUpdateCredential(true, null, credentialForm, err);
                });
        } else {
            this.authAlertMessage = MessageConstants.PASSWORD_OR_KEY_REQUIRED;
            this.authErrorFlag = true;
            this.btnState = ClrLoadingState.DEFAULT;
        }
    }

    public updateMachineCredential(credentialForm: NgForm) {
        this.authErrorFlag = false;
        if (typeof this.keyFileToUpload != 'undefined' || (typeof credentialForm.value.password != 'undefined' && credentialForm.value.password != "" && credentialForm.value.password != null)) {
            this.endpointService.updateRemoteMachineCredential(credentialForm.value, this.keyFileToUpload).subscribe(
                res => {
                    this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_UPDATE, credentialForm, null);
                }, err => {
                    this.finishAddUpdateCredential(true, null, credentialForm, err);
                });
        } else {
            this.authAlertMessage = MessageConstants.PASSWORD_OR_KEY_REQUIRED;
            this.authErrorFlag = true;
            this.btnState = ClrLoadingState.DEFAULT;
        }
    }

    public cancelForm(credentialForm: NgForm) {
        this.outputMessage.emit(this.outMessageData);
        this.awsCredentialModal = false;
        this.azureCredentialModal = false;
        this.vcenterCredentialModal = false;
        this.k8sCredentialModal = false;
        this.machineCredentialModal = false;
        this.databaseCredentialModal = false;
        this.authErrorFlag = false;
        credentialForm.reset();
    }

    private finishAddUpdateCredential(isErrorMessageVal, alertMessageVal, credentialForm: NgForm, errData) {
        this.outMessageData.credentialData = credentialForm.value;
        this.outMessageData.isErrorMessage = isErrorMessageVal;
        if (alertMessageVal != null) {
            this.outMessageData.alertMessage = alertMessageVal;
        } else {
            var errorAlert = errData.error.description;
            if (errorAlert === undefined) {
                errorAlert = errData.error.error;
            }
            this.outMessageData.alertMessage = errorAlert;
        }
        this.outputMessage.emit(this.outMessageData);
        this.awsCredentialModal = false;
        this.vcenterCredentialModal = false;
        this.k8sCredentialModal = false;
        this.machineCredentialModal = false;
        this.databaseCredentialModal = false;
        this.btnState = ClrLoadingState.DEFAULT;
        credentialForm.reset();
    }

    public populateDbPort(dbType: string) {
        if (dbType !== undefined && dbType !== "") {
            if (dbType === CommonConstants.POSTGRES) {
                this.databaseCredentialFormData.dbPort = 5432;
            }
            if (dbType === CommonConstants.MONGODB) {
                this.databaseCredentialFormData.dbPort = 27017;
            }
            if (dbType === CommonConstants.CASSANDRA) {
                this.databaseCredentialFormData.dbPort = 9042;
            }
        }
    }

    public addDatabaseCredential(credentialForm: NgForm) {
        this.endpointService.addDatabaseCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_ADD, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }

    public updateDatabaseCredential(credentialForm: NgForm) {
        this.endpointService.updateDatabaseCredential(credentialForm.value).subscribe(
            res => {
                this.finishAddUpdateCredential(false, credentialForm.value.name + MessageConstants.CREDENTIAL_UPDATE, credentialForm, null);
            }, err => {
                this.finishAddUpdateCredential(true, null, credentialForm, err);
            });
    }
}

