import { Component, OnInit } from '@angular/core';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';

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

    public isLoading: boolean = true;

    ngOnInit() {
        this.getCredentials();
    }

    public populateCredentialForm(credentialData: any) {
        this.credentialFormData = credentialData;
    }

    public getCredentials() {
        this.isLoading = true;
        this.endpointService.getCredentials().subscribe(
            res => {
                if (res.code) {
                    this.credentials = [];
                    this.isLoading = false;
                } else {
                    this.credentials = res;
                    this.isLoading = false;
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

    public deleteCredential(credential) {
        this.errorFlag = false;
        this.successFlag = false;
        if (confirm('Do you want to delete: ' + credential.name + ' credential?')) {
            this.isLoading = true;
            this.endpointService.deleteCredential(credential.name).subscribe(
                res => {
                    this.getCredentials();
                    this.alertMessage = credential.name + ' credential deleted successfully!';
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getCredentials();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

}
