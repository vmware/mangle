import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { FaultService } from "../../fault.service";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";
import { CommonUtils } from "src/app/shared/commonUtils";
import { CommonConstants } from "src/app/common/common.constants";
import { FaultCommons } from "../../fault.commons";

@Component({
  selector: "app-db-transaction-error",
  templateUrl: "./transactionerror.component.html"
})
export class DbTransactionErrorComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.DATABASE];
  public supportedDatabaseTypes: any = [CommonConstants.POSTGRES];
  public dbTransactionErrorCodes: any = [];

  public faultFormData: any = {
    "tableName": null,
    "percentage": 1,
    "errorCode": null,
    "injectionHomeDir": null,
    "dockerArguments": {
      "containerName": null
    },
    "endpointName": null,
    "k8sArguments": {
      "containerName": null,
      "enableRandomInjection": true,
      "podLabels": null
    },
    "schedule": {
      "cronExpression": null,
      "timeInMilliseconds": null,
      "description": null
    },
    "timeoutInMilliseconds": 1
  };

  constructor(private faultService: FaultService, endpointService: EndpointService,
    private router: Router, private dataService: DataService, commonUtils: CommonUtils) {
    super(endpointService, commonUtils);
  }

  ngOnInit() {
    this.getAllEndpoints();
    if (this.dataService.sharedData != null) {
      this.populateFaultData();
    }
  }

  public populateFaultData() {
    this.faultFormData.tableName = this.dataService.sharedData.tableName;
    this.faultFormData.errorCode = this.dataService.sharedData.errorCode;
    this.faultFormData.percentage = this.dataService.sharedData.percentage;
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.timeoutInMilliseconds = this.dataService.sharedData.timeoutInMilliseconds;
    if (this.dataService.sharedData.randomEndpoint != null) {
      this.faultFormData.randomEndpoint = this.dataService.sharedData.randomEndpoint;
    }
    if (this.dataService.sharedData.dockerArguments != null) {
      this.faultFormData.dockerArguments = this.dataService.sharedData.dockerArguments;
      this.dockerHidden = false;
    }
    if (this.dataService.sharedData.k8sArguments != null) {
      this.faultFormData.k8sArguments = this.dataService.sharedData.k8sArguments;
      this.k8sHidden = false;
    }
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    if (this.faultFormData.errorCode != null) {
      this.dbTransactionErrorCodes.push(this.faultFormData.errorCode);
    }
    this.dataService.sharedData = null;
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public setScheduleCron(eventVal) {
    this.faultFormData.schedule.cronExpression = eventVal;
    this.setSubmitButton();
    this.cronModal = false;
  }

  public setSubmitButton() {
    if ((this.faultFormData.schedule.cronExpression !== "" && this.faultFormData.schedule.cronExpression != null) ||
      (this.faultFormData.schedule.timeInMilliseconds != null && this.faultFormData.schedule.timeInMilliseconds !== 0)) {
      this.disableSchedule = false;
      this.disableRun = true;
    } else {
      this.disableSchedule = true;
      this.disableRun = false;
    }
  }

  public executeTransactionErrorFault(faultData: any) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeTransactionErrorFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        if (res.taskData.schedule == null) {
          this.router.navigateByUrl(CommonConstants.REQUESTS_PROCESSED_URL);
        } else {
          this.router.navigateByUrl(CommonConstants.REQUESTS_SCHEDULED_URL);
        }
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
        this.runBtnState = ClrLoadingState.DEFAULT;
      });
  }

  public getTransactionErrorCode() {
    if (this.epDbType !== undefined && this.epDbType !== "") {
      this.faultService.getTransactionErrorCode(this.epDbType).subscribe(
        res => {
          if (res.code) {
            this.dbTransactionErrorCodes = [];
          } else {
            this.dbTransactionErrorCodes = res.content;
          }
        }, err => {
          this.dbTransactionErrorCodes = [];
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
        });
    }
  }

}
