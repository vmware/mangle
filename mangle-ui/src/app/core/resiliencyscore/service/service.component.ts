import { Component, OnInit } from '@angular/core';
import { ClrLoadingState } from '@clr/angular';
import { ResiliencyscoreService } from '../resiliencyscore.service';
import { MessageConstants } from 'src/app/common/message.constants';
import { QueryService } from '../query/query.service';

@Component({
    selector: 'app-service',
    templateUrl: './service.component.html'
})
export class ServiceComponent implements OnInit {

    public isLoading = true;
    public allServices: any = [];
    public alertMessage: string;
    public isErrorMessage: boolean;
    public serviceFormData: any;
    public disableSubmit = false;
    public serviceDatagrid = false;
    public addEdit: string;

    public selectedQuery: any;
    public selectedServices: any = [];
    public serviceForm = true;
    public allQueries: any = [];

    public tagsData: any = {};
    public tagsModal: boolean;
    public tagsSet = false;

    constructor(private resiliencyscoreService: ResiliencyscoreService, private queryService: QueryService) {
    }

    ngOnInit() {
        this.getAllServices();
        this.getAllQueries();
    }

    public getAllServices() {
        this.isLoading = true;
        this.resiliencyscoreService.getAllServices().subscribe(
            res => {
                if (res != null) {
                    this.allServices = res.content;
                } else {
                    this.allServices = [];
                }
                this.isLoading = false;
            }, err => {
                this.allServices = [];
                this.isLoading = false;
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public getAllQueries() {
        this.isLoading = true;
        this.queryService.getAllQueries().subscribe(
            res => {
                if (res != null) {
                    this.allQueries = res.content;
                } else {
                    this.allQueries = [];
                }
                this.isLoading = false;
            }, err => {
                this.allQueries = [];
                this.isLoading = false;
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public populateServiceForm(serviceData: any) {
        this.disableSubmit = false;
        this.serviceFormData = serviceData;
        if (serviceData.tags != null) {
            this.tagsData = serviceData.tags;
            this.tagsSet = true;
        } else {
            this.tagsData = {};
            this.tagsSet = false;
        }
    }

    public serviceDefaultValue: any = {
        "name": null,
        "queryNames": null,
        "tags": null
    };

    public addOrUpdateService(service: any, action: string) {
        if (this.tagsData !== {}) {
            service.tags = this.tagsData;
            this.tagsData = {};
        }
        if (action === 'Add') {
            this.addService(service);
        } else {
            this.updateService(service);
        }
    }

    public addService(service: any) {
        this.isLoading = true;
        this.resiliencyscoreService.addService(service).subscribe(
            res => {
                this.getAllServices();
                this.isErrorMessage = false;
                this.alertMessage = service.name + MessageConstants.SERVICE_CREATED;
                this.isLoading = false;
            }, err => {
                this.getAllServices();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateService(service: any) {
        this.isLoading = true;
        this.resiliencyscoreService.updateService(service).subscribe(
            res => {
                this.getAllServices();
                this.isErrorMessage = false;
                this.alertMessage = service.name + MessageConstants.SERVICE_UPDATED;
                this.isLoading = false;
            }, err => {
                this.getAllServices();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateTags(tagsVal) {
        this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
        this.tagsSet = true;
    }

    public removeTag(tagKeyToRemove) {
        delete this.tagsData[tagKeyToRemove];
        if (JSON.stringify(this.tagsData) === "{}") {
            this.tagsSet = false;
        }
    }

    public deleteService(serviceName: any) {
        const selectedMetricConfigNames = [];
        if (serviceName == undefined) {
            this.alertMessage = MessageConstants.SERVICE_NOT_SELECTED;
        } else {
            if (confirm(MessageConstants.DELETE_CONFIRM + serviceName.name + MessageConstants.QUESTION_MARK)) {
                this.isLoading = true;
                this.resiliencyscoreService.deleteService(serviceName.name).subscribe(
                    res => {
                        this.getAllServices();
                        this.isErrorMessage = false;
                        this.alertMessage = serviceName.name + MessageConstants.SERVICE_DELETED;
                        this.isLoading = false;
                    }, err => {
                        this.getAllServices();
                        this.isErrorMessage = true;
                        this.alertMessage = err.error.description;
                        this.isLoading = false;
                        if (this.alertMessage === undefined) {
                            this.alertMessage = err.error.error;
                        }
                    });
            }
        }
    }

}
