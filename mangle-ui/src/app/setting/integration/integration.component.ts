import { Component, OnInit } from '@angular/core';
import { MetricProviderService } from './metric-provider.service';
import { ClrLoadingState } from '@clr/angular';

@Component({
    selector: 'app-integration',
    templateUrl: './integration.component.html',
    styleUrls: ['./integration.component.css']
})
export class IntegrationComponent implements OnInit {

    public metricProviderList: any;

    public isLoading: boolean = true;
    public alertMessage: String;
    public successFlag: boolean;
    public errorFlag: boolean;

    public addEdit: string;

    public wavefrontFormData: any;
    public datadogFormData: any;

    public tagsData: any = {};

    public updateBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public wavefrontDefaultValues: any = {
        "id": null,
        "name": null,
        "metricProviderType": "WAVEFRONT",
        "waveFrontConnectionProperties": {
            "wavefrontInstance": null,
            "wavefrontAPIToken": null,
            "waveFrontProxy": null,
            "waveFrontProxyPort": 0,
            "source": null,
            "staticTags": null,
            "prefix": null
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
    }

    public populateWavefrontForm(wavefrontData) {
        if (wavefrontData.waveFrontConnectionProperties.staticTags != null) {
            this.tagsData = wavefrontData.waveFrontConnectionProperties.staticTags;
        } else {
            this.tagsData = {};
        }
        this.wavefrontFormData = wavefrontData;
    }

    public populateDatadogForm(datadogData) {
        if (datadogData.datadogConnectionProperties.staticTags != null) {
            this.tagsData = datadogData.datadogConnectionProperties.staticTags;
        } else {
            this.tagsData = {};
        }
        this.datadogFormData = datadogData;
    }

    public getMetricProviders() {
        this.isLoading = true;
        this.metricProviderService.getMetricProviders().subscribe(
            res => {
                if (res.code) {
                    this.metricProviderList = [];
                    this.isLoading = false;
                } else {
                    this.metricProviderList = res;
                    this.getActiveMetricProvider();
                    //this.isLoading = false;
                }
            }, err => {
                this.metricProviderList = [];
                this.isLoading = false;
                this.alertMessage = err.error.description;
                this.errorFlag = true;
            }
        );
    }

    public addOrUpdateMetricProviders(metricProvider) {
        delete metricProvider["isActive"];
        if (this.tagsData != {} && metricProvider.metricProviderType == "WAVEFRONT") {
            metricProvider.waveFrontConnectionProperties.staticTags = this.tagsData;
        }
        if (this.tagsData != {} && metricProvider.metricProviderType == "DATADOG") {
            metricProvider.datadogConnectionProperties.staticTags = this.tagsData;
        }
        if (metricProvider.id == null) {
            this.addMetricProvider(metricProvider);
        } else {
            this.updateMetricProvider(metricProvider);
        }
    }

    public addMetricProvider(metricProvider) {
        delete metricProvider["id"];
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.metricProviderService.addMetricProvider(metricProvider).subscribe(
            res => {
                this.getMetricProviders();
                this.alertMessage = 'Metric Provider added successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getMetricProviders();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateMetricProvider(metricProvider) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.metricProviderService.updateMetricProvider(metricProvider).subscribe(
            res => {
                this.getMetricProviders();
                this.alertMessage = 'Metric Provider updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getMetricProviders();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public deleteMetricProvider(metricProvider) {
        this.errorFlag = false;
        this.successFlag = false;
        if (confirm('Do you want to delete: ' + metricProvider.name + ' metric provider?')) {
            this.isLoading = true;
            this.metricProviderService.deleteMetricProvider(metricProvider.name).subscribe(
                res => {
                    this.getMetricProviders();
                    this.alertMessage = metricProvider.name + ' metric provider deleted successfully!';
                    this.successFlag = true;
                    this.isLoading = false;
                }, err => {
                    this.getMetricProviders();
                    this.alertMessage = err.error.description;
                    this.errorFlag = true;
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
                for (var i = 0; i < this.metricProviderList.length; i++) {
                    if (res[0] != null && res[0].name == this.metricProviderList[i].name) {
                        this.metricProviderList[i].isActive = true;
                    } else {
                        this.metricProviderList[i].isActive = false;
                    }
                }
                this.isLoading = false;
            }, err => {
                for (var i = 0; i < this.metricProviderList.length; i++) {
                    this.metricProviderList[i].isActive = false;
                }
                console.error(err.error.description);
                this.isLoading = false;
            });
    }

    public updateMetricProviderStatus(metricProvider) {
        this.errorFlag = false;
        this.successFlag = false;
        this.isLoading = true;
        this.metricProviderService.updateMetricProviderStatus(metricProvider.name).subscribe(
            res => {
                this.getMetricProviders();
                this.alertMessage = 'Metric provider status updated successfully!';
                this.successFlag = true;
                this.isLoading = false;
            }, err => {
                this.getMetricProviders();
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.isLoading = false;
            });
    }

    public updateMetricCollectionStatus(status) {
        this.updateBtnState = ClrLoadingState.LOADING;
        this.errorFlag = false;
        this.successFlag = false;
        this.metricProviderService.updateMetricCollectionStatus(status).subscribe(
            res => {
                this.alertMessage = 'Update metric collection status successfull!';
                this.successFlag = true;
                this.updateBtnState = ClrLoadingState.DEFAULT;
            }, err => {
                this.alertMessage = err.error.description;
                this.errorFlag = true;
                this.updateBtnState = ClrLoadingState.DEFAULT;
            });
    }

    public updateStaticTags(tagsVal) {
        this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
    }

    public removeStaticTag(tagKeyToRemove) {
        delete this.tagsData[tagKeyToRemove];
    }

}
