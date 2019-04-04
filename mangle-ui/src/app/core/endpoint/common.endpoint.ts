import { OnInit } from '@angular/core';
import { EndpointService } from './endpoint.service';
import { ClrLoadingState } from '@clr/angular';

export class CommonEndpoint implements OnInit {

    public epDatagrid = false;
    public epForm = true;
    public epType: string;
    public epFormData: any;
    public k8sFileToUpload: any;
    public keyFileToUpload: any;

    public endpoints: any;
    public credentials: any;

    public tagsData: any = {};

    public addEdit: string;

    public errorFlag = false;
    public successFlag = false;
    public alertMessage: string;

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
                this.alertMessage = 'Endpoint added successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getEndpoints();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateEndpoint(endpoint) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.endpointService.updateEndpoint(endpoint).subscribe(
            res => {
                this.getEndpoints();
                this.alertMessage = 'Endpoint updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getEndpoints();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteEndpoint(endpoint) {
        this.errorFlag = false;
        this.successFlag = false;
        if (confirm('Do you want to delete: ' + endpoint.name + ' endpoint?')) {
            this.isLoading = true;
            this.endpointService.deleteEndpoint(endpoint.name).subscribe(
                res => {
                    this.getEndpoints();
                    this.alertMessage = endpoint.name + ' endpoint deleted successfully!';
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getEndpoints();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.isLoading = false;
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
            });
    }

    public getPvtKeyFiles(fileToUploadEvent) {
        this.keyFileToUpload = fileToUploadEvent.target.files[0];
    }

    public addMachineCredential(credential) {
        delete credential["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.endpointService.addRemoteMachineCredential(credential, this.keyFileToUpload).subscribe(
            res => {
                this.getCredentials();
                this.alertMessage = 'Credentials added successfully!';
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            });
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
                this.alertMessage = 'Credentials added successfully!';
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            });
    }

    public addVcenterCredential(credential) {
        delete credential["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.endpointService.addVcenterCredential(credential).subscribe(
            res => {
                this.getCredentials();
                this.alertMessage = 'Credentials added successfully!';
                this.successFlag = true;
            }, err => {
                this.getCredentials();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            });
    }

    public testEndpointConnection(endpoint) {
        delete endpoint["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.testEndpointBtnState = ClrLoadingState.LOADING;
        this.endpointService.testEndpointConnection(endpoint).subscribe(
            res => {
                if (res.code) {
                    this.testEndpointBtnState = ClrLoadingState.ERROR;
                    this.alertMessage = 'Test endpoint connection was failed. ' + res.description;
                    this.errorFlag = true;
                } else {
                    this.testEndpointBtnState = ClrLoadingState.SUCCESS;
                    this.disableSubmit = false;
                    this.alertMessage = 'Test endpoint connection successful!';
                    this.successFlag = true;
                }
            }, err => {
                this.testEndpointBtnState = ClrLoadingState.ERROR;
                this.alertMessage = 'Test endpoint connection was failed. ' + err.error.description;
                this.errorFlag = true;
            });
    }

}
