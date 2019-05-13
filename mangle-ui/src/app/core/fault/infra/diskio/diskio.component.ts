import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';

@Component({
  selector: 'app-diskio-infra',
  templateUrl: './diskio.component.html',
  styleUrls: ['./diskio.component.css']
})
export class DiskioInfraComponent implements OnInit {

  public errorFlag = false;
  public successFlag = false;
  public alertMessage: string;

  public cronModal: boolean = false;

  public disableSchedule: boolean = true;
  public disableRun: boolean = false;

  public tagsData: any = {};

  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;
  public selectedSchedulePrev: string = "";

  public endpoints: any = [];
  public dockerHidden: boolean = true;
  public k8sHidden: boolean = true;

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public faultFormData: any = {
    "endpointName": null,
    "ioSize": 0,
    "targetDir": null,
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
    "timeoutInMilliseconds": 0
  };

  public searchedEndpoints: any = [];

  constructor(private faultService: FaultService, private endpointService: EndpointService, private router: Router) {

  }

  ngOnInit() {
    this.errorFlag = false;
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        if (res.code) {
          this.endpoints = [];
        } else {
          this.endpoints = res;
        }
      }, err => {
        this.endpoints = [];
        this.alertMessage = err.error.description;
        this.errorFlag = true;
      });
  }

  public searchEndpoint(searchKeyWord) {
    this.searchedEndpoints = [];
    for (var i = 0; i < this.endpoints.length; i++) {
      if (this.endpoints[i].name.indexOf(searchKeyWord) > -1) {
        this.searchedEndpoints.push(this.endpoints[i]);
      }
    }
  }

  public setEndpointVal(endpointVal) {
    this.faultFormData.endpointName = endpointVal;
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

  public executeDiskIOFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    this.errorFlag = false;
    this.successFlag = false;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.faultService.executeDiskIOFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        if (res.taskData.schedule == null) {
          this.router.navigateByUrl('core/requests/processed');
        } else {
          this.router.navigateByUrl('core/requests/scheduled');
        }
      }, err => {
        this.alertMessage = err.error.description;
        this.errorFlag = true;
        if (this.alertMessage === undefined) {
          this.alertMessage = err.error.error;
        }
        this.runBtnState = ClrLoadingState.DEFAULT;
      });
  }

}
