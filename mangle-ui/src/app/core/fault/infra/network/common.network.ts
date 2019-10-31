import { Router } from '@angular/router';
import { FaultService } from '../../fault.service';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { CommonConstants } from 'src/app/common/common.constants';
import { CommonUtils } from 'src/app/shared/commonUtils';

export class CommonNetwork {

  public alertMessage: string;
  public isErrorMessage: boolean;

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public tagsData: any = {};
  public originalTagsData: any = {};
  public searchedEndpoints: any = [];
  public endpoints: any = [];
  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;
  public selectedSchedulePrev: string = "";
  public cronModal: boolean = false;

  public disableSchedule: boolean = true;
  public disableRun: boolean = false;
  public faultData: any = {};
  public faultFormData: any = {
    "endpointName": null,
    "nicName": null,
    "timeoutInMilliseconds": null,
    "injectionHomeDir": null,
    "faultOperation": null,
    "schedule": {
      "cronExpression": null,
      "timeInMilliseconds": null,
      "description": null
    },
  };
  private commonUtils: CommonUtils = new CommonUtils();

  constructor(private faultService: FaultService, private router: Router, private faultOperation: string) {


  }

  public executeNetworkFault(faultData) {
    faultData.faultOperation = this.faultOperation;
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.faultService.executeNetworkFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        if (res.taskData.schedule == null) {
          this.router.navigateByUrl('core/requests/processed');
        } else {
          this.router.navigateByUrl('core/requests/scheduled');
        }
      }, err => {
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
        if (this.alertMessage === undefined) {
          this.isErrorMessage= true;
          this.alertMessage = err.error.error;
        }
        this.runBtnState = ClrLoadingState.DEFAULT;
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
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        this.tagsData = this.commonUtils.getTagsData(this.originalTagsData,this.endpoints[i].tags);
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
  public populateFaultData(dataService: DataService) {
    this.faultFormData.nicName = dataService.sharedData.nicName;
    this.faultFormData.injectionHomeDir = dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = dataService.sharedData.endpointName;
    this.faultFormData.timeoutInMilliseconds = dataService.sharedData.timeoutInMilliseconds;
    if (dataService.sharedData.faultOperation == CommonConstants.NETWORK_DELAY_MILLISECONDS) {
      this.faultFormData.latency = dataService.sharedData.latency;
    }
    else {
      this.faultFormData.percentage = dataService.sharedData.percentage;
    }
    if (dataService.sharedData.tags != null) {
      this.tagsData = dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(dataService.sharedData.tags));
    }
    this.faultFormData.faultOperation = dataService.sharedData.faultOperation;
    dataService.sharedData = null;
  }

}


