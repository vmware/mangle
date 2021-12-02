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
  selector: "app-delete-k8s-resource",
  templateUrl: "./delete-k8s-resource.component.html"
})
export class DeleteK8SResourceComponent extends FaultCommons implements OnInit {

  public resourceNameHidden = true;
  public resourceLabelsHidden = true;
  public resourceListHidden = true;
  public resourceListAvailable = true;
  public resourceLabelsData: any = {};
  public supportedEpTypes: any = [CommonConstants.K8S_CLUSTER];
  public k8sResourceTypes: any = ["POD", "NODE", "SERVICE", "DEPLOYMENT", "STATEFULSET",
    "SECRET", "DAEMONSET", "CONFIGMAP", "JOB", "REPLICASET", "REPLICATIONCONTROLLER", "PV", "PVC"];
  public resourceLabelsModal: boolean;
  public inputType: any = null;

  public faultFormData: any = {
    "endpointName": null,
    "resourceType": null,
    "resourceName": null,
    "resourceLabels": {},
    "randomInjection": false,
    "injectionHomeDir": null,
    "schedule": {
      "cronExpression": null,
      "timeInMilliseconds": null,
      "description": null
    }
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
    this.faultFormData.resourceType = this.dataService.sharedData.resourceType;
    this.faultFormData.resourceName = this.dataService.sharedData.resourceName;
    this.faultFormData.resourceLabels = this.dataService.sharedData.resourceLabels;
    this.resourceLabelsData = this.dataService.sharedData.resourceLabels;
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
    this.resourceListHidden = true;
    if (selectedResource === "resourceName") {
      this.resourceNameHidden = false;
    }
    if (selectedResource === "resourceLabels") {
      this.resourceLabelsHidden = false;
    }
    if(selectedResource == "resourceList") {
      this.resourceListHidden = false;
    }
  }

  public updateResourceLabels(resourceLabelsVal) {
    this.resourceLabelsData[resourceLabelsVal.resourceLabelsKey] = resourceLabelsVal.resourceLabelsValue;
  }

  public removeResourceLabels(resourceLabelsKeyToRemove) {
    delete this.resourceLabelsData[resourceLabelsKeyToRemove];
  }

  public executeK8SDeleteResourceFault(faultData) {
    if (!this.resourceNameHidden || !this.resourceListHidden) {
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
    this.faultService.executeK8SDeleteResourceFault(faultData).subscribe(
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

  public setResourceType(resourceType : String){
    this.faultFormData.resourceType = resourceType;
    this.inputType = null;
    if (resourceType == "POD" || resourceType == null){
      this.resourceListAvailable = true;
      this.resourceNameHidden = true;
      this.resourceLabelsHidden = true;
      this.resourceListHidden = true;
    }else{
      this.resourceListAvailable = false;
      this.resourceNameHidden = true;
      this.resourceLabelsHidden = true;
      this.resourceListHidden = true;
      this.k8sResources = [];
      this.faultFormData.resourceName = null;
      this.getAllResources(this.faultFormData.endpointName,resourceType);
    }
  }

  public setK8sResourceVal(resourceVal){
    this.faultFormData.resourceName = resourceVal;
  }

  public resetResourceType(){
    this.setResourceType(null);
    this.inputType = null;
  }

}
