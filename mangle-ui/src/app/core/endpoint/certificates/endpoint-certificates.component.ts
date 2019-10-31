import { Component, OnInit } from '@angular/core';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-endpoint-certificates',
    templateUrl: './endpoint-certificates.component.html'
})
export class EndpointCertificatesComponent implements OnInit {

    constructor(private endpointService: EndpointService) { }

    public certificates: any;
    public certificatesFormData: any;
    public dockerCaCertToUpload: any;
    public dockerServerCertToUpload: any;
    public dockerPrivateKeyToUpload: any;

    public alertMessage: string;
    public isErrorMessage: boolean;

    public addEdit: string;

    public submitBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public isLoading: boolean = true;

    ngOnInit() {
        this.getCertificates();
    }

    public populateCertificateForm(certificatesData: any) {
        this.certificatesFormData = certificatesData;
    }

    public getCertificates() {
        this.isLoading = true;
        this.endpointService.getCertificates().subscribe(
            res => {
                if (res.code) {
                    this.certificates = [];
                    this.isLoading = false;
                } else {
                    this.certificates = res;
                    this.isLoading = false;
                }
            }, err => {
                this.certificates = [];
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
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

    public addUpdateDockerCertificates(certificates) {
        if (certificates.id == null) {
            this.addDockerCertificates(certificates);
        } else {
            this.updateDockerCertificates(certificates);
        }
    }

    public addDockerCertificates(certificates) {
        this.isLoading = true;
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

    public updateDockerCertificates(certificates) {
        this.isLoading = true;
        this.endpointService.updateDockerCertificates(certificates, this.dockerCaCertToUpload, this.dockerServerCertToUpload, this.dockerPrivateKeyToUpload).subscribe(
            res => {
                this.getCertificates();
                this.isErrorMessage = false;
                this.alertMessage = certificates.name + MessageConstants.CERTIFICATES_UPDATE;
            }, err => {
                this.getCertificates();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public deleteCertificates(certificates) {
        this.isLoading = true;
        if (confirm(MessageConstants.DELETE_CONFIRM + certificates.name + MessageConstants.QUESTION_MARK)) {
            this.isLoading = true;
            this.endpointService.deleteCertificates(certificates.name).subscribe(
                res => {
                    this.getCertificates();
                    this.isErrorMessage = false;
                    this.alertMessage = certificates.name + MessageConstants.CERTIFICATES_DELETE;
                }, err => {
                    this.getCertificates();
                    this.isErrorMessage = true;
                    this.alertMessage = err.error.description;
                    if (this.alertMessage === undefined) {
                        this.alertMessage = err.error.error;
                    }
                });
        } else {
            // Do nothing!
        }
    }

}
