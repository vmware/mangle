import { Component, OnInit } from "@angular/core";
import { Router } from "@angular/router";
import { FaultService } from "../../../../fault.service";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";
import { CommonUtils } from "src/app/shared/commonUtils";
import { CommonConstants } from "src/app/common/common.constants";
import { FaultCommons } from "src/app/core/fault/fault.commons";

@Component({
  selector: "app-azure-vm-storage",
  templateUrl: "./azure-vm-storage.component.html"
})
export class AzureVMStorageComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.AZURE];
  public storageFaultTypes: any = ["DETACH_DISKS"];
  public azureTagsData: any = {};
  public azureTagsModal: boolean;

  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "azureTags": {},
    "randomInjection": true,
    "selectRandomDisks": true
  };

  constructor(private faultService: FaultService, endpointService: EndpointService, private router: Router, private dataService: DataService, commonUtils: CommonUtils) {
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
    this.azureTagsData = this.dataService.sharedData.azureTags;
    this.faultFormData.randomInjection = this.dataService.sharedData.randomInjection;
    this.faultFormData.selectRandomDisks = this.dataService.sharedData.selectRandomDisks;
    this.faultFormData.fault = this.dataService.sharedData.fault;
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public updateAzureTags(azureTagsVal) {
    this.azureTagsData[azureTagsVal.tagKey] = azureTagsVal.tagValue;
  }

  public removeAzureTag(azureTagKeyToRemove) {
    delete this.azureTagsData[azureTagKeyToRemove];
  }

  public executeAzureVMStorageFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    if (this.azureTagsData !== {}) {
      faultData.azureTags = this.azureTagsData;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeAzureVMStorageFault(faultData).subscribe(
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
