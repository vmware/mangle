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

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

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
        this.errorFlag = false;
        this.endpointService.getCertificates().subscribe(
            res => {
                if (res.code) {
                    this.certificates = [];
                } else {
                    this.certificates = res;
                }
            }, err => {
                this.certificates = [];
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            });
    }

    public getEndpoints() {
        this.isLoading = true;
        this.errorFlag = false;
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
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
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
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.endpointService.addEndpoint(endpoint).subscribe(
            res => {
                this.getEndpoints();
                this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_ADD;
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getEndpoints();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateEndpoint(endpoint) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.endpointService.updateEndpoint(endpoint).subscribe(
            res => {
                this.getEndpoints();
                this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_UPDATE;
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getEndpoints();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public deleteEndpoint(endpoint) {
        this.errorFlag = false;
        this.successFlag = false;
        if (confirm(MessageConstants.DELETE_CONFIRM + endpoint.name + MessageConstants.QUESTION_MARK)) {
            this.isLoading = true;
            this.endpointService.deleteEndpoint(endpoint.name).subscribe(
                res => {
                    this.getEndpoints();
                    this.alertMessage = endpoint.name + MessageConstants.ENDPOINT_DELETE;
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getEndpoints();
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

    public updateTags(tagsVal) {
        this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
    }

    public removeTag(tagKeyToRemove) {
        delete this.tagsData[tagKeyToRemove];
    }

    public getCredentials() {
        this.errorFlag = false;
        this.endpointService.getCredentials().subscribe(
            res => {
                if (res.code) {
                    this.credentials = [];
                } else {
                    this.credentials = res;
                }
            }, err => {
                this.credentials = [];
                this.alertMessage = err.error.description;
                this.errorFlag = true;
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
        this.errorFlag = false;
        this.successFlag = false;
        if (typeof this.keyFileToUpload != undefined || (typeof credential.password != undefined && credential.password != "" && credential.password != null)) {
            this.endpointService.addRemoteMachineCredential(credential, this.keyFileToUpload).subscribe(
                res => {
                    this.getCredentials();
                    this.alertMessage = credential.name + MessageConstants.CREDENTIAL_ADD;
                    this.successFlag = true;
                    this.submitBtnState = ClrLoadingState.DEFAULT;
                    this.machineCredential = false;
                    credentialForm.reset();
                }, err => {
                    this.getCredentials();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
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

    public addVcenterCredential(credential) {
        delete credential["id"];
        this.errorFlag = false;
        this.successFlag = false;
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
        this.errorFlag = false;
        this.successFlag = false;
        this.endpointService.addDockerCertificates(certificates, this.dockerCaCertToUpload, this.dockerServerCertToUpload, this.dockerPrivateKeyToUpload).subscribe(
            res => {
                this.getCertificates();
                this.alertMessage = certificates.name + MessageConstants.CERTIFICATES_ADD;
                this.successFlag = true;
            }, err => {
                this.getCertificates();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public testEndpointConnection(isFormValid, endpoint) {
        if (isFormValid) {
            this.errorFlag = false;
            this.successFlag = false;
            this.testEndpointBtnState = ClrLoadingState.LOADING;
            this.endpointService.testEndpointConnection(endpoint).subscribe(
                res => {
                    if (res.code) {
                        this.testEndpointBtnState = ClrLoadingState.ERROR;
                        this.alertMessage = res.description;
                        this.errorFlag = true;
                    } else {
                        this.testEndpointBtnState = ClrLoadingState.SUCCESS;
                        this.disableSubmit = false;
                        this.alertMessage = MessageConstants.TEST_CONNECTION;
                        this.successFlag = true;
                    }
                }, err => {
                    this.testEndpointBtnState = ClrLoadingState.ERROR;
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                });
        }
    }

}
