import { Component, OnInit } from '@angular/core';
import { ClrLoadingState } from '@clr/angular';
import { SettingService } from '../setting.service';
import { MessageConstants } from 'src/app/common/message.constants';

@Component({
    selector: 'app-resiliencyscore-metric-config',
    templateUrl: './resiliencyscore-metric-config.component.html'
})
export class ResiliencyScoreMetricConfigComponent implements OnInit {

    public metricConfigInfo: any = [];
    public isLoading = true;
    public metricConfigDatagrid = false;
    public metricConfigForm = false;
    public disableSubmit = false;
    public testSlackBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public alertMessage: string;
    public isErrorMessage: boolean;
    public selectedMetricConfigs: any = [];
    public channelsData: any = [];
    public addEdit: string;
    public enableSlack: boolean = true;
    public hideEnableDisableButtons: boolean = false;
    public metricConfigData: any;
    public metricGranularityList = ['s', 'm', 'h', 'd'];


    constructor(private settingService: SettingService) {
    }

    ngOnInit() {
        this.getMetricConfigInfo();
    }

    public getMetricConfigInfo() {
        this.isLoading = true;
        this.settingService.getResiliencyScoreMetricConfig().subscribe(
            res => {
                if (res != null) {
                    this.metricConfigInfo = res;
                    this.isLoading = false;
                } else {
                    this.metricConfigInfo = [];
                    this.isLoading = false;
                }
            }, err => {
                this.metricConfigInfo = [];
                this.isLoading = false;
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public populateMetricConfigForm(metricConfig: any) {
        this.disableSubmit = false;
        this.metricConfigData = metricConfig;
    }

    public resiliencyScoreMetricConfigDefaultData: any = {
        "name": null,
        "metricName": null,
        "metricSource": null,
        "testReferenceWindow": null,
        "resiliencyCalculationWindow": null,
        "metricQueryGranularity": null
    };

    public addOrUpdateMetricConfig(metricConfig: any, action: string) {

        if (action === 'Add') {
            this.addMetricConfig(metricConfig);
        } else {
            this.updateMetricConfig(metricConfig);
        }
    }

    public addMetricConfig(metricConfig: any) {
        this.isLoading = true;
        this.settingService.addResiliencyScoreMetricConfig(metricConfig).subscribe(
            res => {
                this.getMetricConfigInfo();
                this.isErrorMessage = false;
                this.alertMessage = metricConfig.name + MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG_ADD;
                this.isLoading = false;
            }, err => {
                this.getMetricConfigInfo();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }

    public updateMetricConfig(metricConfig: any) {
        this.isLoading = true;
        this.settingService.updateResiliencyScoreMetricConfig(metricConfig).subscribe(
            res => {
                this.getMetricConfigInfo();
                this.isErrorMessage = false;
                this.alertMessage = metricConfig.name + MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG_UPDATE_SUCCESSFUL;
                this.isLoading = false;
            }, err => {
                this.getMetricConfigInfo();
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
            });
    }


    public deleteMetricConfig(metricConfigs: any) {
        const selectedMetricConfigNames = [];
        if (metricConfigs == undefined) {
            this.alertMessage = MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG_NOT_SELECTED;
        } else {
            if (confirm(MessageConstants.DELETE_CONFIRM + selectedMetricConfigNames + MessageConstants.QUESTION_MARK)) {
                this.isLoading = true;
                this.settingService.deleteResiliencyScoreMetricConfig(metricConfigs.name).subscribe(
                    res => {
                        this.getMetricConfigInfo();
                        this.isErrorMessage = false;
                        this.alertMessage = MessageConstants.RESILIENCY_SCORE_METRIC_CONFIG_DELETE;
                        this.isLoading = false;
                    }, err => {
                        this.getMetricConfigInfo();
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
