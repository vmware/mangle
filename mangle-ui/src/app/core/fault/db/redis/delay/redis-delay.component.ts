import { Component, AfterViewInit } from "@angular/core";
import { Router } from "@angular/router";
import { FaultService } from "../../../fault.service";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";
import { CommonUtils } from "src/app/shared/commonUtils";
import { CommonConstants } from "src/app/common/common.constants";
import { FaultCommons } from "../../../fault.commons";

@Component({
  selector: "app-db-redis-delay",
  templateUrl: "./redis-delay.component.html"
})
export class RedisDbDelayComponent extends FaultCommons implements AfterViewInit {

  public supportedEpTypes: any = [CommonConstants.REDIS_FI_PROXY];

  public faultFormData: any = {
    "delay": 1,
    "percentage": 1,
    "endpointName": null
  };

  constructor(private faultService: FaultService, endpointService: EndpointService,
    private router: Router, private dataService: DataService, commonUtils: CommonUtils) {
    super(endpointService, commonUtils);
  }

  ngAfterViewInit() {
    this.getAllEndpoints();
    if (this.dataService.sharedData != null) {
      this.populateFaultData();
    }
  }

  public populateFaultData() {
    this.faultFormData.delay = this.dataService.sharedData.delay;
    this.faultFormData.percentage = this.dataService.sharedData.percentage;
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public executeRedisDelayFault(faultData: any) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeRedisDelayFault(faultData).subscribe(
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
