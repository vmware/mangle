import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {FaultService} from "../fault.service";
import {EndpointService} from "../../endpoint/endpoint.service";
import {CommonConstants} from "src/app/common/common.constants";
import {FaultCommons} from "../fault.commons";
import {CommonUtils} from "src/app/shared/commonUtils";

@Component({
  selector: "app-custom-fault",
  templateUrl: "./custom-fault.component.html"
})
export class CustomFaultComponent extends FaultCommons implements OnInit {

  public pluginList: any;
  public searchedPlugins: any = [];
  public faultNameMaps: any = [];
  public faultParamHidden = true;
  public faultParams: any = {};

  public supportedEpTypes: any = [CommonConstants.MACHINE, CommonConstants.K8S_CLUSTER,
    CommonConstants.DOCKER, CommonConstants.VCENTER, CommonConstants.AWS];

  public faultFormData: any = {
    "pluginId": null,
    "faultName": null,
    "endpointName": null,
    "faultParameters": {},
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

  constructor(private faultService: FaultService, private router: Router, private dataService: DataService,
              endpointService: EndpointService, commonUtils: CommonUtils) {
    super(endpointService, commonUtils);
  }

  ngOnInit() {
    this.getPluginDetails();
    this.getAllEndpoints();
  }

  public getPluginDetails() {
    this.faultService.getPluginDetails().subscribe(
      res => {
        this.pluginList = res.content;
      }, err => {
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
        this.pluginList = [];
      });
    if (this.dataService.sharedData != null) {
      this.populateFaultData();
    }
  }

  public populateFaultData() {
    this.faultParamHidden = true;
    this.faultFormData.pluginId = this.dataService.sharedData.pluginMetaInfo.pluginId;
    this.faultFormData.faultName = this.dataService.sharedData.pluginMetaInfo.faultName;
    this.faultNameMaps.push({"faultName": this.dataService.sharedData.pluginMetaInfo.faultName});
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.faultParameters = this.dataService.sharedData.faultParameters;
    this.faultParams = this.dataService.sharedData.faultParameters;
    this.faultParamHidden = false;
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
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public searchPlugin(searchKeyWord) {
    this.searchedPlugins = [];
    for (let i = 0; i < this.pluginList.length; i++) {
      if (this.pluginList[i].pluginId.indexOf(searchKeyWord) > -1) {
        this.searchedPlugins.push(this.pluginList[i]);
      }
    }
  }

  public populateFaultNameMaps(pluginIdVal) {
    this.faultFormData.pluginId = pluginIdVal;
    for (let j = 0; j < this.pluginList.length; j++) {
      if (this.pluginList[j].pluginId === pluginIdVal) {
        this.faultNameMaps = this.pluginList[j].customFaultDescriptorMap;
      }
    }
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public populateEndpointsAndFaultParams(faultNameVal) {
    this.faultParamHidden = true;
    this.faultService.getCustomFaultJson(this.faultFormData.pluginId, faultNameVal).subscribe(
      res => {
        this.faultParams = res.faultParameters;
        this.supportedEpTypes = res.supportedEndpoints;
        this.faultParamHidden = false;
      }, err => {
        this.faultParams = {};
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public executeCustomFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeCustomFault(faultData).subscribe(
      res => {
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

  public setScheduleCron(eventVal) {
    this.faultFormData.schedule.cronExpression = eventVal;
    this.setSubmitButton();
    this.cronModal = false;
  }

}
