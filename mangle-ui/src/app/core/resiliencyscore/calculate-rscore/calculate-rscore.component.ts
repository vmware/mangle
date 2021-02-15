import { Component, OnInit } from '@angular/core';
import { Router } from "@angular/router";
import { MessageConstants } from 'src/app/common/message.constants';
import { ResiliencyscoreService } from '../resiliencyscore.service';
import { CalculateResiliencyScoreService } from './calculate-rscore-service';
import { CommonConstants } from "src/app/common/common.constants";
import { ClrLoadingState } from "@clr/angular";

@Component({
    selector: 'app-calculaterscore',
    templateUrl: './calculate-rscore.component.html'
})
export class CalculateResiliencyScoreComponent implements OnInit {

    public epDatagrid = false;
    public cronModal = false;
    public epForm = true;
    public epType: string;
    public services: any;
    public isLoading: boolean;
    public isErrorMessage: boolean;
    public alertMessage: any;
    public disableSchedule: boolean = true;
    public disableRun = false;
    public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

    public rscoreData: any = {
        'serviceName': '',
        "schedule": {
            "cronExpression": null,
            "timeInMilliseconds": null,
            "description": null
        }
    }

    ngOnInit() {
        this.getAllServices();
    }
    constructor(private resiliencyscoreService: ResiliencyscoreService, private calculateResiliencyScoreService: CalculateResiliencyScoreService, private router: Router) {
    }

    public calculateResiliencyScore(resiliencyScoreData: any) {
        this.runBtnState = ClrLoadingState.LOADING;
        this.calculateResiliencyScoreService.triggerResiliencyScoreCalculation(resiliencyScoreData).subscribe(
            res => {
                this.isErrorMessage = false;
                this.alertMessage = MessageConstants.RESILINECY_SCORE_SUBMITTED_FOR_SERVICE + resiliencyScoreData.serviceName;
                if (res.taskData.schedule == null) {
                    this.router.navigateByUrl(CommonConstants.REQUESTS_PROCESSED_URL);
                } else {
                    this.router.navigateByUrl(CommonConstants.REQUESTS_SCHEDULED_URL);
                }
            }, err => {
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
                this.isLoading = false;
                if (this.alertMessage === undefined) {
                    this.alertMessage = err.error.error;
                }
                this.runBtnState = ClrLoadingState.DEFAULT;
            });
    }

    public getAllServices() {
        this.resiliencyscoreService.getAllServices().subscribe(
            res => {
                if (res != null) {
                    this.services = res.content;
                    this.isLoading = false;
                } else {
                    this.services = [];
                    this.isLoading = false;
                }
            }, err => {
                this.services = [];
                this.isLoading = false;
                this.isErrorMessage = true;
                this.alertMessage = err.error.description;
            });
    }

    public setSubmitButton() {
        if ((this.rscoreData.schedule.cronExpression !== "" && this.rscoreData.schedule.cronExpression != null)
            || (this.rscoreData.schedule.timeInMilliseconds != null && this.rscoreData.schedule.timeInMilliseconds !== 0)) {
            this.disableSchedule = false;
            this.disableRun = true;
        } else {
            this.disableSchedule = true;
            this.disableRun = false;
        }
    }

    public viewCronModal(modalVal) {
        this.cronModal = modalVal;
    }

    public setScheduleCron(eventVal) {
        this.rscoreData.schedule.cronExpression = eventVal;
        this.setSubmitButton();
        this.cronModal = false;
    }
}