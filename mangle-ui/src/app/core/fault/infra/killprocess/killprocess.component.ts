import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { CommonUtils } from 'src/app/shared/commonUtils';
import { CommonConstants } from 'src/app/common/common.constants';

@Component({
  selector: 'app-killprocess',
  templateUrl: './killprocess.component.html'
})
export class KillprocessComponent implements OnInit {

  public alertMessage: string;
  public isErrorMessage: boolean;

  public cronModal: boolean = false;

  public disableSchedule: boolean = true;
  public disableRun: boolean = false;

  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;
  public selectedSchedulePrev: string = "";

  public tagsData: any = {};
  public originalTagsData: any = {};

  public endpoints: any = [];
  public dockerHidden: boolean = true;
  public k8sHidden: boolean = true;

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public dockerContainers: any = [];

  public processIdentifierHidden: boolean = true;
  public processIdHidden: boolean = true;

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

  public searchedEndpoints: any = [];
  public searchedContainers: any = [];

  constructor(private faultService: FaultService, private endpointService: EndpointService, private router: Router, private dataService: DataService, private commonUtils: CommonUtils) {

  }

  ngOnInit() {
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        if (res.code) {
          this.endpoints = [];
        } else {
          this.endpoints = res;
        }
      }, err => {
        this.endpoints = [];
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
      });
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
    this.dataService.sharedData = null;
  }

  public searchEndpoint(searchKeyWord) {
    this.searchedEndpoints = [];
    for (var i = 0; i < this.endpoints.length; i++) {
      if (this.endpoints[i].name.indexOf(searchKeyWord) > -1) {
        this.searchedEndpoints.push(this.endpoints[i]);
      }
    }
  }

  public setKillUsingVal(selectedKillUsing) {
    this.processIdentifierHidden = true;
    this.processIdHidden = true;
    if (selectedKillUsing == "processIdentifier") {
      this.processIdentifierHidden = false;
    }
    if (selectedKillUsing == "processId") {
      this.processIdHidden = false;
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

  public setEndpointVal(endpointVal) {
    this.faultFormData.endpointName = endpointVal;
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
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

  public updateTags(tagsVal) {
    this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
  }

  public removeTag(tagKeyToRemove) {
    delete this.tagsData[tagKeyToRemove];
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

  public displayEndpointFields(endpointNameVal) {
    this.dockerHidden = true;
    this.k8sHidden = true;
    this.tagsData = {};
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        this.tagsData = this.commonUtils.getTagsData(this.originalTagsData, this.endpoints[i].tags);
        if (this.endpoints[i].endPointType == 'DOCKER') {
          this.dockerHidden = false;
        }
        if (this.endpoints[i].endPointType == 'K8S_CLUSTER') {
          this.k8sHidden = false;
        }
      }
    }
  }

  public setScheduleCron(eventVal) {
    this.faultFormData.schedule.cronExpression = eventVal;
    this.setSubmitButton();
    this.cronModal = false;
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

  public executeKillProcessFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    if (this.processIdentifierHidden == true) {
      faultData.killAll = true;
      delete faultData["processIdentifier"];
    } else {
      delete faultData["processId"];
    }
    this.faultService.executeKillProcessFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        if (res.taskData.schedule == null) {
          this.router.navigateByUrl(CommonConstants.REQUESTS_PROCESSED_URL);
        } else {
          this.router.navigateByUrl(CommonConstants.REQUESTS_SCHEDULED_URL);
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

}
