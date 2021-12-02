import { Component, AfterViewInit } from "@angular/core";
import { Router } from "@angular/router";
import { FaultService } from "../../fault.service";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";
import { CommonUtils } from "src/app/shared/commonUtils";
import { CommonConstants } from "src/app/common/common.constants";
import { FaultCommons } from "../../fault.commons";

@Component({
  selector: "app-networkpartition-infra",
  templateUrl: "./networkpartition.component.html"
})
export class NetworkPartitionComponent extends FaultCommons implements AfterViewInit {

  public supportedEpTypes: any = [CommonConstants.MACHINE];
  public hostList: any = [];
  public host: any = "";

  public faultFormData: any = {
    "endpointName": null,
    "hosts": [],
    "injectionHomeDir": null,
    "dockerArguments": {
      "containerName": null
    },
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

  constructor(private faultService: FaultService, endpointService: EndpointService, private router: Router,
    private dataService: DataService, commonUtils: CommonUtils) {
    super(endpointService, commonUtils);
  }

  ngAfterViewInit() {
    this.getAllEndpoints();
    if (this.dataService.sharedData != null) {
      this.populateFaultData();
    }
  }

  public populateFaultData() {
    this.hostList = this.dataService.sharedData.hosts;
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.timeoutInMilliseconds = this.dataService.sharedData.timeoutInMilliseconds;
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

  public setContainerVal(containerVal: any) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public setScheduleCron(eventVal: any) {
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

  public executeNetworkPartitionFault(faultData: any) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    if (this.host.trim().length > 0 && this.hostList.indexOf(this.host) < 0) {
      this.hostList.push(this.host);
    }
    if (this.hostList !== []) {
      faultData.hosts = this.hostList;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeNetworkPartitionFault(faultData).subscribe(
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

  public updateHostList() {
    if (this.host.trim().length > 0 && this.hostList.indexOf(this.host) < 0) {
      var regEx = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
      if(this.host.match(regEx)){
        this.hostList.push(this.host);
      } else {
        this.isErrorMessage = true;
        this.alertMessage = "Invalid IP address";
      }
    }
    this.host = "";
  }

  public removeHost(hostToRemove: string) {
    const index = this.hostList.indexOf(hostToRemove);
    if (index > -1) {
      this.hostList.splice(index, 1);
    }
  }
}
