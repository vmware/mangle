import {Router} from "@angular/router";
import {FaultService} from "../../fault.service";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {CommonConstants} from "src/app/common/common.constants";
import {CommonUtils} from "src/app/shared/commonUtils";
import {FaultCommons} from "../../fault.commons";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {OnInit} from "@angular/core";

export class CommonNetwork extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.MACHINE];

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

  ngOnInit() {
  }

  constructor(endpointService: EndpointService, private faultService: FaultService, private router: Router,
              private faultOperation: string, commonUtils: CommonUtils) {
    super(endpointService, commonUtils);
  }

  public executeNetworkFault(faultData) {
    faultData.faultOperation = this.faultOperation;
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeNetworkFault(faultData).subscribe(
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
          this.isErrorMessage = true;
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

  public populateFaultData(dataService: DataService) {
    this.faultFormData.nicName = dataService.sharedData.nicName;
    this.faultFormData.injectionHomeDir = dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = dataService.sharedData.endpointName;
    this.faultFormData.timeoutInMilliseconds = dataService.sharedData.timeoutInMilliseconds;
    if (dataService.sharedData.randomEndpoint != null) {
      this.faultFormData.randomEndpoint = dataService.sharedData.randomEndpoint;
    }
    if (dataService.sharedData.faultOperation === CommonConstants.NETWORK_DELAY_MILLISECONDS) {
      this.faultFormData.latency = dataService.sharedData.latency;
    } else {
      this.faultFormData.percentage = dataService.sharedData.percentage;
    }
    if (dataService.sharedData.tags != null) {
      this.tagsData = dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(dataService.sharedData.tags));
    }
    this.faultFormData.faultOperation = dataService.sharedData.faultOperation;
    this.populateFaultNotifiers(dataService);
    dataService.sharedData = null;
  }

}


