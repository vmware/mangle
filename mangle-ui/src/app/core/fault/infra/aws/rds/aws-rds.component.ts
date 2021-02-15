import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { FaultService } from "../../../fault.service";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";
import { CommonUtils } from "src/app/shared/commonUtils";
import { CommonConstants } from "src/app/common/common.constants";
import { FaultCommons } from "src/app/core/fault/fault.commons";

@Component({
  selector: "app-aws-rds",
  templateUrl: "./aws-rds.component.html"
})
export class AwsRDSComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.AWS];
  public faultTypes: any = ["STOP_INSTANCES", "REBOOT_INSTANCES", "FAILOVER_INSTANCES", "CONNECTION_LOSS"];
  public dbIdentifiers: any = [];
  public dbIdentifier: any = "";
  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "dbIdentifiers": [],
    "randomInjection": true
  };

  constructor(private faultService: FaultService, endpointService: EndpointService, private router: Router,
    private dataService: DataService, commonUtils: CommonUtils) {
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
    this.dbIdentifiers = this.dataService.sharedData.dbIdentifiers;
    this.faultFormData.randomInjection = this.dataService.sharedData.randomInjection;
    this.faultFormData.fault = this.dataService.sharedData.fault;
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public updateDbIdentifiers() {
    if (this.dbIdentifier.trim().length > 0) {
      this.dbIdentifiers.push(this.dbIdentifier);
      this.dbIdentifier = "";
    }
  }

  public removeDbIdentifier(deIdentifierToRemove) {
    const index = this.dbIdentifiers.indexOf(deIdentifierToRemove);
    if (index > -1) {
      this.dbIdentifiers.splice(index, 1);
    }
  }

  public executeAwsRDSFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }

    if (this.dbIdentifier.trim().length > 0 && this.dbIdentifiers.indexOf(this.dbIdentifier) < 0) {
      this.dbIdentifiers.push(this.dbIdentifier);
    }
    if (this.dbIdentifiers !== []) {
      faultData.dbIdentifiers = this.dbIdentifiers;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeAwsRDSFault(faultData).subscribe(
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
