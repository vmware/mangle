import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { FaultService } from "../../fault.service";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";
import { CommonConstants } from "src/app/common/common.constants";
import { CommonUtils } from "src/app/shared/commonUtils";
import { FaultCommons } from "../../fault.commons";

@Component({
  selector: "app-kernelpanic",
  templateUrl: "./kernelpanic.component.html"
})
export class KernelPanicComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.MACHINE];
  public isKernelPanicFault = true;

  public faultFormData: any = {
    "endpointName": null,
    "injectionHomeDir": null,
    "schedule": {
      "cronExpression": null,
      "timeInMilliseconds": null,
      "description": null
    }
  };

  constructor(private faultService: FaultService, endpointService: EndpointService, private router: Router,
    private dataService: DataService, commonUtils: CommonUtils) {
    super(endpointService, commonUtils);
  }

  ngOnInit() {
    this.getAllEndpoints();
    if (this.dataService.sharedData != null) {
      this.populateFaultData();
    }
  }

  public populateFaultData() {
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    if (this.dataService.sharedData.randomEndpoint != null) {
      this.faultFormData.randomEndpoint = this.dataService.sharedData.randomEndpoint;
    }
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public setScheduleCron(eventVal) {
    this.faultFormData.schedule.cronExpression = eventVal;
    this.setSubmitButton();
    this.cronModal = false;
  }

  public setSubmitButton() {
    if ((this.faultFormData.schedule.cronExpression !== "" && this.faultFormData.schedule.cronExpression != null)
      || (this.faultFormData.schedule.timeInMilliseconds != null && this.faultFormData.schedule.timeInMilliseconds !== 0)) {
      this.disableSchedule = false;
      this.disableRun = true;
    } else {
      this.disableSchedule = true;
      this.disableRun = false;
    }
  }

  public executeKernelPanicFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeKernelPanicFault(faultData).subscribe(
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

}
