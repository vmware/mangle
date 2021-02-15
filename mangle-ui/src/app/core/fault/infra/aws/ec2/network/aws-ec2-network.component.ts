import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {FaultService} from "../../../../fault.service";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {CommonUtils} from "src/app/shared/commonUtils";
import {CommonConstants} from "src/app/common/common.constants";
import {FaultCommons} from "src/app/core/fault/fault.commons";

@Component({
  selector: "app-aws-ec2-network",
  templateUrl: "./aws-ec2-network.component.html"
})
export class AwsEC2NetworkComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.AWS];
  public networkFaultTypes: any = ["BLOCK_ALL_NETWORK_TRAFFIC"];
  public awsTagsData: any = {};
  public awsTagsModal: boolean;

  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "awsTags": {},
    "randomInjection": true
  };

  constructor(private faultService: FaultService, endpointService: EndpointService,
              private router: Router, private dataService: DataService, commonUtils: CommonUtils) {
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
    this.awsTagsData = this.dataService.sharedData.awsTags;
    this.faultFormData.randomInjection = this.dataService.sharedData.randomInjection;
    this.faultFormData.fault = this.dataService.sharedData.fault;
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public updateAwsTags(awsTagsVal) {
    this.awsTagsData[awsTagsVal.tagKey] = awsTagsVal.tagValue;
  }

  public removeAwsTag(awsTagKeyToRemove) {
    delete this.awsTagsData[awsTagKeyToRemove];
  }

  public executeAwsEC2NetworkFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    if (this.awsTagsData !== {}) {
      faultData.awsTags = this.awsTagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeAwsEC2NetworkFault(faultData).subscribe(
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
