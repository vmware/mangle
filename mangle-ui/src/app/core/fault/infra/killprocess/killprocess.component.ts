import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {FaultService} from "../../fault.service";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {CommonUtils} from "src/app/shared/commonUtils";
import {CommonConstants} from "src/app/common/common.constants";
import {FaultCommons} from "../../fault.commons";

@Component({
  selector: "app-killprocess",
  templateUrl: "./killprocess.component.html"
})
export class KillprocessComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.MACHINE, CommonConstants.K8S_CLUSTER, CommonConstants.DOCKER];

  public processIdentifierHidden = true;
  public processIdHidden = true;

  public faultFormData: any = {
    "endpointName": null,
    "processIdentifier": null,
    "processId": null,
    "killAll": false,
    "remediationCommand": null,
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
    this.faultFormData.processIdentifier = this.dataService.sharedData.processIdentifier;
    this.faultFormData.killAll = this.dataService.sharedData.killAll;
    this.faultFormData.processId = this.dataService.sharedData.processId;
    this.faultFormData.remediationCommand = this.dataService.sharedData.remediationCommand;
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    if (this.dataService.sharedData.randomEndpoint != null) {
      this.faultFormData.randomEndpoint = this.dataService.sharedData.randomEndpoint;
    }
    if (this.dataService.sharedData.processIdentifier != null) {
      document.getElementById("processIdentifier").click();
    } else {
      document.getElementById("processId").click();
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
    this.dataService.sharedData = null;
  }

  public setKillUsingVal(selectedKillUsing) {
    this.processIdentifierHidden = true;
    this.processIdHidden = true;
    if (selectedKillUsing === "processIdentifier") {
      this.processIdentifierHidden = false;
    }
    if (selectedKillUsing === "processId") {
      this.processIdHidden = false;
    }
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
    if ((this.faultFormData.schedule.cronExpression !== "" && this.faultFormData.schedule.cronExpression != null)
      || (this.faultFormData.schedule.timeInMilliseconds != null && this.faultFormData.schedule.timeInMilliseconds !== 0)) {
      this.disableSchedule = false;
      this.disableRun = true;
    } else {
      this.disableSchedule = true;
      this.disableRun = false;
    }
  }

  public executeKillProcessFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    if (this.processIdentifierHidden === true) {
      faultData.killAll = true;
      delete faultData["processIdentifier"];
    } else {
      delete faultData["processId"];
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeKillProcessFault(faultData).subscribe(
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
