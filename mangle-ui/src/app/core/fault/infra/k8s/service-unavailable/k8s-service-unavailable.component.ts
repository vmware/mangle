import {Component, Inject, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {FaultService} from "../../../fault.service";
import {MessageConstants} from "src/app/common/message.constants";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {DOCUMENT} from "@angular/common";
import {CommonUtils} from "src/app/shared/commonUtils";
import {CommonConstants} from "src/app/common/common.constants";
import {FaultCommons} from "../../../fault.commons";

@Component({
  selector: "app-k8s-service-unavailable",
  templateUrl: "./k8s-service-unavailable.component.html"
})
export class K8SServiceUnavailableComponent extends FaultCommons implements OnInit {

  public resourceNameHidden = true;
  public resourceLabelsHidden = true;
  public resourceLabelsData: any = {};
  public resourceLabelsModal: boolean;

  public supportedEpTypes: any = [CommonConstants.K8S_CLUSTER];

  public faultFormData: any = {
    "endpointName": null,
    "resourceName": null,
    "resourceLabels": {},
    "randomInjection": true
  };

  constructor(private faultService: FaultService, endpointService: EndpointService, private router: Router,
              private dataService: DataService, @Inject(DOCUMENT) document, commonUtils: CommonUtils) {
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
    this.faultFormData.resourceName = this.dataService.sharedData.resourceName;
    this.faultFormData.resourceLabels = this.dataService.sharedData.resourceLabels;
    if (this.dataService.sharedData.resourceLabels != null) {
      this.resourceLabelsData = this.dataService.sharedData.resourceLabels;
    } else {
      this.resourceLabelsData = {};
    }

    this.faultFormData.appContainerName = this.dataService.sharedData.appContainerName;
    this.faultFormData.randomInjection = this.dataService.sharedData.randomInjection;
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    if (this.dataService.sharedData.resourceName != null) {
      document.getElementById("resourceName").click();
    } else {
      document.getElementById("resourceLabels").click();
    }
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public setResourceVal(selectedResource) {
    this.resourceNameHidden = true;
    this.resourceLabelsHidden = true;
    if (selectedResource === "resourceName") {
      this.resourceNameHidden = false;
    }
    if (selectedResource === "resourceLabels") {
      this.resourceLabelsHidden = false;
    }
  }

  public updateResourceLabels(resourceLabelsVal) {
    this.resourceLabelsData[resourceLabelsVal.resourceLabelsKey] = resourceLabelsVal.resourceLabelsValue;
  }

  public removeResourceLabels(resourceLabelsKeyToRemove) {
    delete this.resourceLabelsData[resourceLabelsKeyToRemove];
  }

  public executeK8SServiceUnavailableFault(faultData) {
    if (!this.resourceNameHidden) {
      delete faultData["resourceLabels"];
      faultData.randomInjection = true;
    } else {
      faultData.resourceLabels = this.resourceLabelsData;
      if (JSON.stringify(faultData.resourceLabels) === JSON.stringify({})) {
        this.isErrorMessage = true;
        this.alertMessage = MessageConstants.RESOURCE_LABEL_REQUIRED;
        return false;
      }
      delete faultData["resourceName"];
    }
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.runBtnState = ClrLoadingState.LOADING;
    this.faultService.executeK8SServiceUnavailable(faultData).subscribe(
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
