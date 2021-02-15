import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {FaultService} from "../../fault.service";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {CommonConstants} from "src/app/common/common.constants";
import {CommonUtils} from "src/app/shared/commonUtils";
import {FaultCommons} from "../../fault.commons";

@Component({
  selector: "app-javamethodlatency",
  templateUrl: "./javamethodlatency.component.html"
})
export class JavaMethodLatencyComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.MACHINE, CommonConstants.K8S_CLUSTER, CommonConstants.DOCKER];

  public faultFormData: any = {
    "injectionHomeDir": null,
    "className": null,
    "latency": null,
    "methodName": null,
    "ruleEvent": null,

    "dockerArguments": {
      "containerName": null
    },
    "endpointName": null,
    "jvmProperties": {
      "javaHomePath": null,
      "jvmprocess": null,
      "port": 9091,
      "user": null
    },
    "k8sArguments": {
      "containerName": null,
      "enableRandomInjection": true,
      "podLabels": null
    }
  };

  constructor(private faultService: FaultService, endpointService: EndpointService, private router: Router, private dataService: DataService, commonUtils: CommonUtils) {
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
    this.faultFormData.latency = this.dataService.sharedData.latency;
    this.faultFormData.className = this.dataService.sharedData.className;
    this.faultFormData.methodName = this.dataService.sharedData.methodName;
    this.faultFormData.ruleEvent = this.dataService.sharedData.ruleEvent;
    this.faultFormData.jvmProperties = this.dataService.sharedData.jvmProperties;
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
    this.dataService.sharedData = null;
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public setSubmitButton() {
    this.disableRun = false;
  }

  public executeJavaMethodLatencyFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeJavaMethodLatencyFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        this.router.navigateByUrl(CommonConstants.REQUESTS_PROCESSED_URL);
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
