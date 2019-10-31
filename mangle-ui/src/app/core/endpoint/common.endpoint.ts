import { OnInit } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { NgForm } from '@angular/forms';
import { MessageConstants } from 'src/app/common/message.constants';

export class CommonEndpoint implements OnInit {

    public epDatagrid = false;
    public epForm = true;
    public epType: string;
    public epFormData: any;
    public k8sFileToUpload: any;
    public keyFileToUpload: any;
    public dockerCaCertToUpload: any;
    public dockerServerCertToUpload: any;
    public dockerPrivateKeyToUpload: any;

    public endpoints: any;
    public credentials: any;
    public certificates: any;
    public tagsData: any = {};

    public searchedCredentials: any = [];

    public passwordHidden: boolean = true;
    public privateKeyHidden: boolean = true;

    public addEdit: string;

    public alertMessage: string;
    public isErrorMessage: boolean;

    public authErrorFlag = false;
    public authAlertMessage: string;

    public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public machineCredential: boolean = false;

    public isLoading: boolean = true;

    public testEndpointBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public disableSubmit: boolean = true;

    constructor(private endpointService: EndpointService, endpointType: string) {
        this.epType = endpointType;
    }

    ngOnInit() { }

    public populateEndpointForm(endpoint) {
        this.disableSubmit = true;
        if (endpoint.tags != null) {
            this.tagsData = endpoint.tags;
        } else {
            this.tagsData = {};
        }
        this.epFormData = endpoint;
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

    public getCertificates() {
        this.endpointService.getCertificates().subscribe(
            res => {
                if (res.code) {
                    this.certificates = [];
                } else {
                    this.certificates = res;
                }
            }, err => {
                this.certificates = [];
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public getEndpoints() {
        this.isLoading = true;
        this.endpointService.getEndpoints(this.epType).subscribe(
            res => {
                if (res.code) {
                    this.endpoints = [];
                    this.isLoading = false;
                } else {
                    this.endpoints = res;
                    this.isLoading = false;
                }
            }, err => {
                this.endpoints = [];
                this.isLoading = false;
                if (err.error.code === 'FI506') {
                    this.isErrorMessage = true;
                    this.alertMessage = err.error.description;
                }
            }
        );
    }

    public addOrUpdateEndpoint(endpoint) {
        if (this.tagsData != {}) {
            endpoint.tags = this.tagsData;
            this.tagsData = {};
        }
        this.testEndpointBtnState = ClrLoadingState.DEFAULT;
        if (endpoint.id == null) {
            this.addEndpoint(endpoint);
        } else {
            this.updateEndpoint(endpoint);
        }
    }

    public addEndpoint(endpoint) {
        delete endpoint["id"];
        this.isLoading = true;
        this.endpointService.addEndpoint(endpoint).subscribe(
            res => {
                this.getEndpoints();
                this.isErrorMessage = false;
                this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_ADD;
                this.isLoading = false;
            }, err => {
                this.getEndpoints();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateEndpoint(endpoint) {
        this.isLoading = true;
        this.endpointService.updateEndpoint(endpoint).subscribe(
            res => {
                this.getEndpoints();
                this.isErrorMessage = false;
                this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_UPDATE;
                this.isLoading = false;
            }, err => {
                this.getEndpoints();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public deleteEndpoint(endpoint) {
        if (confirm(MessageConstants.DELETE_CONFIRM + endpoint.name + MessageConstants.QUESTION_MARK)) {
            this.isLoading = true;
            this.endpointService.deleteEndpoint(endpoint.name).subscribe(
                res => {
                    this.getEndpoints();
                    this.isErrorMessage = false;
                    this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_DELETE;
                    this.isLoading = false;
                }, err => {
                    this.getEndpoints();
                    this.isErrorMessage = true;
                    this.alertMessage = err.error.description;
                    this.isLoading = false;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                });
        } else {
            // Do nothing!
        }
    }

    public updateTags(tagsVal) {
        this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
    }

    public removeTag(tagKeyToRemove) {
        delete this.tagsData[tagKeyToRemove];
    }

    public getCredentials() {
        this.endpointService.getCredentials().subscribe(
            res => {
                if (res.code) {
                    this.credentials = [];
                } else {
                    this.credentials = res;
                }
            }, err => {
                this.credentials = [];
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public searchCredentials(searchKeyWord) {
        this.searchedCredentials = [];
        for (var i = 0; i < this.credentials.length; i++) {
            if (this.credentials[i].name.indexOf(searchKeyWord) > -1) {
                this.searchedCredentials.push(this.credentials[i]);
            }
        }
    }

    public setCredentialVal(credentialVal) {
        this.epFormData.credentialsName = credentialVal;
    }

    public getPvtKeyFiles(fileToUploadEvent) {
        this.keyFileToUpload = fileToUploadEvent.target.files[0];
    }

    public addMachineCredential(credentialForm: NgForm) {
        var credential = credentialForm.value;
        this.authErrorFlag = false;
        this.submitBtnState = ClrLoadingState.LOADING;
        delete credential["id"];
        if (typeof this.keyFileToUpload != undefined || (typeof credential.password != undefined && credential.password != "" && credential.password != null)) {
            this.endpointService.addRemoteMachineCredential(credential, this.keyFileToUpload).subscribe(
                res => {
                    this.getCredentials();
                    this.isErrorMessage = false;
                    this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    this.machineCredential = false;
                    this.epFormData.credentialsName = credential.name;
                    credentialForm.reset();
                }, err => {
                    this.getCredentials();
                    this.isErrorMessage = true;
                    this.alertMessage = err.error.description;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    this.machineCredential = false;
                    credentialForm.reset();
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
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

    public addKubernetesCredential(credential) {
        delete credential["id"];
        this.endpointService.addk8sCredential(credential, this.k8sFileToUpload).subscribe(
            res => {
                this.getCredentials();
                this.isErrorMessage = false;
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                this.epFormData.credentialsName = credential.name;
            }, err => {
                this.getCredentials();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public addVcenterCredential(credential) {
        delete credential["id"];
        this.endpointService.addVcenterCredential(credential).subscribe(
            res => {
                this.getCredentials();
                this.isErrorMessage = false;
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                this.epFormData.credentialsName = credential.name;
            }, err => {
                this.getCredentials();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public addAwsCredential(credential) {
        delete credential["id"];
        this.endpointService.addAwsCredential(credential).subscribe(
            res => {
                this.getCredentials();
                this.isErrorMessage = false;
                this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                this.epFormData.credentialsName = credential.name;
            }, err => {
                this.getCredentials();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public getDockerCaCertFile(fileToUploadEvent) {
        this.dockerCaCertToUpload = fileToUploadEvent.target.files[0];
    }

    public getDockerServerCertFile(fileToUploadEvent) {
        this.dockerServerCertToUpload = fileToUploadEvent.target.files[0];
    }

    public getDockerPrivateKeyFile(fileToUploadEvent) {
        this.dockerPrivateKeyToUpload = fileToUploadEvent.target.files[0];
    }

    public addDockerCertificates(certificates) {
        delete certificates["id"];
        this.endpointService.addDockerCertificates(certificates, this.dockerCaCertToUpload, this.dockerServerCertToUpload, this.dockerPrivateKeyToUpload).subscribe(
            res => {
                this.getCertificates();
                this.isErrorMessage = false;
                this.alertMessage = certificates.name + MessageConstants.CERTIFICATES_ADD;
            }, err => {
                this.getCertificates();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public testEndpointConnection(isFormValid, endpoint) {
        this.alertMessage = null;
        if (isFormValid) {
            this.testEndpointBtnState = ClrLoadingState.LOADING;
            this.endpointService.testEndpointConnection(endpoint).subscribe(
                res => {
                    if (res.code) {
                        this.testEndpointBtnState = ClrLoadingState.ERROR;
                        this.isErrorMessage = true;
                        this.alertMessage = res.description;
                    } else {
                        this.testEndpointBtnState = ClrLoadingState.SUCCESS;
                        this.disableSubmit = false;
                        this.isErrorMessage = false;
                        this.alertMessage = MessageConstants.TEST_CONNECTION;
                    }
                }, err => {
                    this.testEndpointBtnState = ClrLoadingState.ERROR;
                    this.isErrorMessage = true;
                    this.alertMessage = err.error.description;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                });
        }
    }

}
