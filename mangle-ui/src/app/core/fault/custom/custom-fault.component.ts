import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { FaultService } from '../fault.service';
import { EndpointService } from '../../endpoint/endpoint.service';

@Component({
  selector: 'app-custom-fault',
  templateUrl: './custom-fault.component.html'
})
export class CustomFaultComponent implements OnInit {

  public alertMessage: string;
  public isErrorMessage: boolean;

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public pluginList: any;

  public searchedPlugins: any = [];
  public faultNameMaps: any = [];
  public endpoints: any = [];
  public dockerHidden: boolean = true;
  public k8sHidden: boolean = true;
  public faultParamHidden: boolean = true;
  public faultParams: any = {};

  public searchedEndpoints: any = [];
  public tagsData: any = {};
  public cronModal: boolean = false;
  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;
  public selectedSchedulePrev: string = "";
  public disableSchedule: boolean = true;
  public disableRun: boolean = false;
  public dockerContainers: any = [];
  public searchedContainers: any = [];

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

  constructor(private faultService: FaultService, private router: Router, private dataService: DataService, private endpointService: EndpointService) { }

  ngOnInit() {
    this.getPluginDetails();
  }

  public getPluginDetails() {
    this.faultService.getPluginDetails().subscribe(
      res => {
        this.pluginList = res;
      }, err => {
        this.isErrorMessage= true;
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
    this.faultNameMaps.push({ "faultName": this.dataService.sharedData.pluginMetaInfo.faultName });
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.faultParameters = this.dataService.sharedData.faultParameters;
    this.faultParams = this.dataService.sharedData.faultParameters;
    this.faultParamHidden = false;
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
    this.dataService.sharedData = null;
  }

  public searchPlugin(searchKeyWord) {
    this.searchedPlugins = [];
    for (var i = 0; i < this.pluginList.length; i++) {
      if (this.pluginList[i].pluginId.indexOf(searchKeyWord) > -1) {
        this.searchedPlugins.push(this.pluginList[i]);
      }
    }
  }

  public populateFaultNameMaps(pluginIdVal) {
    this.faultFormData.pluginId = pluginIdVal;
    for (var j = 0; j < this.pluginList.length; j++) {
      if (this.pluginList[j].pluginId == pluginIdVal) {
        this.faultNameMaps = this.pluginList[j].customFaultDescriptorMap;
      }
    }
  }

  public searchEndpoint(searchKeyWord) {
    this.searchedEndpoints = [];
    for (var i = 0; i < this.endpoints.length; i++) {
      if (this.endpoints[i].name.indexOf(searchKeyWord) > -1) {
        this.searchedEndpoints.push(this.endpoints[i]);
      }
    }
  }

  public searchContainer(searchKeyWord) {
    this.searchedContainers = [];
    for (var i = 0; i < this.dockerContainers.length; i++) {
      if (this.dockerContainers[i].indexOf(searchKeyWord) > -1) {
        this.searchedContainers.push(this.dockerContainers[i]);
      }
    }
  }

  public getDockerContainers(epType, epName) {
    if (epType == "DOCKER") {
      this.endpointService.getDockerContainers(epName).subscribe(
        res => {
          if (res.code) {
            this.dockerContainers = [];
          } else {
            this.dockerContainers = res;
          }
        }, err => {
          this.dockerContainers = [];
          this.isErrorMessage= true;
          this.alertMessage = err.error.description;
        }
      );
    }
  }

  public setEndpointVal(endpointVal) {
    this.faultFormData.endpointName = endpointVal;
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public populateEndpointsAndFaultParams(faultNameVal) {
    this.faultParamHidden = true;
    this.faultService.getCustomFaultJson(this.faultFormData.pluginId, faultNameVal).subscribe(
      res => {
        this.faultParams = res.faultParameters;
        this.getEndpoints(res.supportedEndpoints);
        this.faultParamHidden = false;
      }, err => {
        this.faultParams = {};
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
      });
  }

  public getEndpoints(supportedEndpoints) {
    this.endpoints=[];
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        for (var s_ep = 0; s_ep < supportedEndpoints.length; s_ep++) {
          for (var g_ep = 0; g_ep < res.length; g_ep++) {
            if (supportedEndpoints[s_ep] == res[g_ep].endPointType) {
              this.endpoints.push(res[g_ep]);
            }
          }
        }
      }, err => {
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
        this.endpoints = [];
      });
  }

  public executeCustomFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
        if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.faultService.executeCustomFault(faultData).subscribe(
      res => {
        if (res.taskData.schedule == null) {
          this.router.navigateByUrl('core/requests/processed');
        } else {
          this.router.navigateByUrl('core/requests/scheduled');
        }
      }, err => {
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
        this.runBtnState = ClrLoadingState.DEFAULT;
      });
  }
  public updateTags(tagsVal) {
    this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
  }

  public removeTag(tagKeyToRemove) {
    delete this.tagsData[tagKeyToRemove];
  }

  public setSubmitButton() {
    if ((this.faultFormData.schedule.cronExpression != "" && this.faultFormData.schedule.cronExpression != null) || (this.faultFormData.schedule.timeInMilliseconds != null && this.faultFormData.schedule.timeInMilliseconds != 0)) {
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

  public displayEndpointFields(endpointNameVal) {
    this.dockerHidden = true;
    this.k8sHidden = true;
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        if (this.endpoints[i].tags != null) {
          this.tagsData = this.endpoints[i].tags;
        } else {
          this.tagsData = {};
        }
        if (this.endpoints[i].endPointType == 'DOCKER') {
          this.dockerHidden = false;
        }
        if (this.endpoints[i].endPointType == 'K8S_CLUSTER') {
          this.k8sHidden = false;
        }
      }
    }
  }

  public setScheduleVal(selectedSchedule) {
    if (this.selectedSchedulePrev == selectedSchedule.value) {
      selectedSchedule.checked = false;
      this.timeInMillisecondsHidden = true;
      this.cronExpressionHidden = true;
      this.descriptionHidden = true;
    } else {
      this.timeInMillisecondsHidden = true;
      this.cronExpressionHidden = true;
      this.descriptionHidden = true;
      if (selectedSchedule.value == "timeInMilliseconds") {
        this.timeInMillisecondsHidden = false;
        this.descriptionHidden = false;
      }
      if (selectedSchedule.value == "cronExpression") {
        this.cronExpressionHidden = false;
        this.descriptionHidden = false;
      }
      this.selectedSchedulePrev = selectedSchedule.value;
    }
  }
}
