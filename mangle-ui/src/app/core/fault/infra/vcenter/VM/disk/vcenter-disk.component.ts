import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {FaultService} from "../../../../fault.service";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {ClrLoadingState} from "@clr/angular";
import {DataService} from "src/app/shared/data.service";
import {CommonUtils} from "src/app/shared/commonUtils";
import {CommonConstants} from "src/app/common/common.constants";
import {FaultCommons} from "../../../../fault.commons";

@Component({
  selector: "app-vcenter-disk",
  templateUrl: "./vcenter-disk.component.html"
})
export class VcenterDiskComponent extends FaultCommons implements OnInit {

  public supportedEpTypes: any = [CommonConstants.VCENTER];
  public diskFaultTypes: any = ["DISCONNECT_DISK"];

  public filtersModal: boolean;
  public filters: any = {};

  public filterKeys = ["dcName", "clusterName", "folder", "resourcePoolName", "host"];

  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "vmDiskId": null,
    "vmName": null,
    "filters": null,
    "enableRandomInjection": true
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
    this.faultFormData.fault = this.dataService.sharedData.fault;
    this.faultFormData.vmDiskId = this.dataService.sharedData.vmDiskId;
    this.faultFormData.vmName = this.dataService.sharedData.vmName;    
    this.faultFormData.enableRandomInjection = this.dataService.sharedData.enableRandomInjection;
    if (this.dataService.sharedData.tags != null) {
      this.tagsData = this.dataService.sharedData.tags;
      this.originalTagsData = JSON.parse(JSON.stringify(this.dataService.sharedData.tags));
    }
    if (this.dataService.sharedData.filters != null) {
      this.filters = this.dataService.sharedData.filters;
      document.getElementById("filters").click();
    }
    if (this.dataService.sharedData.enableRandomInjection != null) {
      document.getElementById("enableRandomInjection").click();
    }
    this.populateFaultNotifiers(this.dataService);
    this.dataService.sharedData = null;
  }

  public executeVcenterDiskFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData !== {}) {
      faultData.tags = this.tagsData;
    }
    if (this.filters !== {}) {
      faultData.filters = this.filters;
    }
    this.addNotifiersInFault(faultData);
    this.faultService.executeVCenterVMDiskFault(faultData).subscribe(
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

  public openFilterModal() {
    this.filtersModal = true;
  }

  public closeFilterModal() {
    this.filtersModal = false;
  }

  public updateFilters(filter) {
    this.filters[filter.filterKey] = filter.filterValue;
    this.filtersModal = false;
  }

  public removeFilter(filterKey) {
    delete this.filters[filterKey];
  }

}
