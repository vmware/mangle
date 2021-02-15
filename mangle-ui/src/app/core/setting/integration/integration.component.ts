import { Component, OnInit } from '@angular/core';
import { MetricProviderService } from './metric-provider.service';
import { ClrLoadingState } from '@clr/angular';
import { MessageConstants } from 'src/app/common/message.constants';
import { CommonConstants } from 'src/app/common/common.constants';

@Component({
    selector: 'app-integration',
    templateUrl: './integration.component.html'
})
export class IntegrationComponent implements OnInit {

    public metricProviderList: any = [];

    public isLoading: boolean = true;

    public alertMessage: string;
    public isErrorMessage: boolean;
    public staticTagsModal: boolean;
    public datadogModal: boolean;
    public wavefrontModal: boolean;

    public testAlertMessage: String;
    public testSuccessFlag: boolean;
    public testErrorFlag: boolean;

    public addEdit: string;

    public wavefrontFormData: any;
    public datadogFormData: any;
    public statusMessage: string;

    public metricCollectionStatus: boolean = false;
    public collectionBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
    public testBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public tagsData: any = {};

    public wavefrontDefaultValues: any = {
        "id": null,
        "name": null,
        "metricProviderType": "WAVEFRONT",
        "waveFrontConnectionProperties": {
            "wavefrontInstance": null,
            "wavefrontAPIToken": null,
            "source": null,
            "staticTags": null
        },
        "isActive": false
    };

    public datadogDefaultValues: any = {
        "id": null,
        "name": null,
        "metricProviderType": "DATADOG",
        "datadogConnectionProperties": {
            "apiKey": null,
            "applicationKey": null,
            "staticTags": null
        },
        "isActive": false
    }

    constructor(private metricProviderService: MetricProviderService) { }

    ngOnInit() {
        this.getMetricProviders();
        this.getMetricCollectionStatus();
    }

    public populateWavefrontForm(wavefrontData) {
        this.testErrorFlag = false;
        this.testSuccessFlag = false;
        if (wavefrontData.waveFrontConnectionProperties.staticTags != null) {
            this.tagsData = wavefrontData.waveFrontConnectionProperties.staticTags;
        } else {
            this.tagsData = {};
        }
        this.wavefrontFormData = wavefrontData;
        this.wavefrontFormData.metricProviderType = CommonConstants.WAVEFRONT;
    }

    public populateDatadogForm(datadogData) {
        this.testErrorFlag = false;
        this.testSuccessFlag = false;
        if (datadogData.datadogConnectionProperties.staticTags != null) {
            this.tagsData = datadogData.datadogConnectionProperties.staticTags;
        } else {
            this.tagsData = {};
        }
        this.datadogFormData = datadogData;
        this.datadogFormData.metricProviderType = CommonConstants.DATADOG;
    }

    public getMetricProviders() {
        this.isLoading = true;
        this.metricProviderService.getMetricProviders().subscribe(
            res => {
                if (res.code) {
                    this.metricProviderList = [];
                    this.isLoading = false;
                } else {
                    this.metricProviderList = res.content;
                    this.getActiveMetricProvider();
                }
            }, err => {
                this.metricProviderList = [];
                this.isLoading = false;
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            }
        );
    }

    public testConnection(isFormValid, metricProvider) {
        if (isFormValid) {
            this.testBtnState = ClrLoadingState.LOADING;
            this.testErrorFlag = false;
            this.testSuccessFlag = false;
            this.isLoading = true;
            this.metricProviderService.testConnection(metricProvider).subscribe(
                res => {
                    this.testAlertMessage = MessageConstants.TEST_CONNECTION;
                    this.testSuccessFlag = true;
                    this.isLoading = false;
                    this.testBtnState = ClrLoadingState.DEFAULT;
                }, err => {
                    this.testAlertMessage = err.error.description;
                    this.testErrorFlag = true;
                    this.isLoading = false;
                    this.testBtnState = ClrLoadingState.DEFAULT;
                });
        }
    }

    public addOrUpdateMetricProviders(metricProvider) {
        delete metricProvider["isActive"];
        if (this.tagsData != {} && metricProvider.metricProviderType == "WAVEFRONT") {
            metricProvider.waveFrontConnectionProperties.staticTags = this.tagsData;
            metricProvider.metricProviderType = "WAVEFRONT";
        }
        if (this.tagsData != {} && metricProvider.metricProviderType == "DATADOG") {
            metricProvider.datadogConnectionProperties.staticTags = this.tagsData;
            metricProvider.metricProviderType = "DATADOG";
        }
        if (metricProvider.id == null) {
            this.addMetricProvider(metricProvider);
        } else {
            this.updateMetricProvider(metricProvider);
        }
    }

    public addMetricProvider(metricProvider) {
        delete metricProvider["id"];
        this.isLoading = true;
        this.metricProviderService.addMetricProvider(metricProvider).subscribe(
            res => {
                this.getMetricProviders();
                this.isErrorMessage = false;
                this.alertMessage = metricProvider.name + MessageConstants.METRIC_PROVIDER_ADD;
                this.isLoading = false;
            }, err => {
                this.getMetricProviders();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
            });
    }

    public updateMetricProvider(metricProvider) {
        this.isLoading = true;
        this.metricProviderService.updateMetricProvider(metricProvider).subscribe(
            res => {
                this.getMetricProviders();
                this.isErrorMessage = false;
                this.alertMessage = metricProvider.name + MessageConstants.METRIC_PROVIDER_UPDATE;
                this.isLoading = false;
            }, err => {
                this.getMetricProviders();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
            });
    }

    public deleteMetricProvider(metricProvider) {
        if (confirm(MessageConstants.DELETE_CONFIRM + metricProvider.name + MessageConstants.QUESTION_MARK)) {
            this.isLoading = true;
            this.metricProviderService.deleteMetricProvider(metricProvider.name).subscribe(
                res => {
                    this.getMetricProviders();
                    this.isErrorMessage = false;
                    this.alertMessage = metricProvider.name + MessageConstants.METRIC_PROVIDER_DELETE;
                    this.isLoading = false;
                    this.getMetricCollectionStatus();
                }, err => {
                    this.getMetricProviders();
                    this.isErrorMessage = true;
                    this.alertMessage = err.error.description;
                    this.isLoading = false;
                });
        } else {
            // Do nothing!
        }
    }

    public getActiveMetricProvider() {
        this.isLoading = true;
        this.metricProviderService.getActiveMetricProvider().subscribe(
            res => {
                res = res.content;
                for (let i = 0; i < this.metricProviderList.length; i++) {
                    if (res !== undefined && res[0] != null && res[0].name === this.metricProviderList[i].name) {
                        this.metricProviderList[i].isActive = true;
                    } else {
                        this.metricProviderList[i].isActive = false;
                    }
                }
                this.isLoading = false;
            }, err => {
                for (let i = 0; i < this.metricProviderList.length; i++) {
                    this.metricProviderList[i].isActive = false;
                }
                console.error(err.error.description);
                this.isLoading = false;
            });
        this.getMetricCollectionStatus();
    }

    public updateMetricProviderStatus(metricProvider) {
        this.statusMessage = MessageConstants.METRIC_PROVIDER_STATUS_UPDATE;
        if (metricProvider.isActive) {
            this.statusMessage = MessageConstants.METRIC_PROVIDER_DiSABLED;
        }
        this.isLoading = true;
        this.metricProviderService.updateMetricProviderStatus(metricProvider.name).subscribe(
            res => {
                this.getMetricProviders();
                this.isErrorMessage = false;
                this.alertMessage = metricProvider.name + this.statusMessage;
                this.isLoading = false;
            }, err => {
                this.getMetricProviders();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
            });
    }

    public updateMetricCollectionStatus() {
        this.collectionBtnState = ClrLoadingState.LOADING;
        this.metricProviderService.updateMetricCollectionStatus(!this.metricCollectionStatus).subscribe(
            res => {
                this.isErrorMessage = false;
                this.alertMessage = MessageConstants.METRIC_PROVIDER_COLLECTION_UPDATE;
                this.getMetricCollectionStatus();
                this.collectionBtnState = ClrLoadingState.DEFAULT;
            }, err => {
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.getMetricCollectionStatus();
                this.collectionBtnState = ClrLoadingState.DEFAULT;
            });
    }

    public getMetricCollectionStatus() {
        this.metricProviderService.getMetricCollectionStatus().subscribe(
            res => {
                this.metricCollectionStatus = res.resultStatus;
            }, err => {
                this.metricCollectionStatus = false;
            });
    }

    public updateStaticTags(tagsVal) {
        this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
    }

    public removeStaticTag(tagKeyToRemove) {
        delete this.tagsData[tagKeyToRemove];
    }

}
