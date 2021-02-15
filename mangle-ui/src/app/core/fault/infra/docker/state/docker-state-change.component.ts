import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {FaultService} from "../../../fault.service";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {CommonUtils} from "src/app/shared/commonUtils";
import {CommonConstants} from "src/app/common/common.constants";
import {FaultCommons} from "../../../fault.commons";

@Component({
  selector: "app-docker-state-change",
  templateUrl: "./docker-state-change.component.html"
})
export class DockerStateChangeComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.DOCKER];
  public dockerFaultNameList: any = ["DOCKER_STOP", "DOCKER_PAUSE"];

  public faultFormData: any = {
    "endpointName": null,
    "dockerFaultName": null,
    "dockerArguments": {
      "containerName": null
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
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.dockerFaultName = this.dataService.sharedData.dockerFaultName;
    this.faultFormData.dockerArguments = this.dataService.sharedData.dockerArguments;
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public executeDockerStateChangeFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeDockerStateChangeFault(faultData).subscribe(
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
