import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { CommonUtils } from 'src/app/shared/commonUtils';

@Component({
  selector: 'app-vcenter-state',
  templateUrl: './vcenter-state.component.html'
})
export class VcenterStateComponent implements OnInit {

  public alertMessage: string;
  public isErrorMessage: boolean;

  public endpoints: any = [];
  public stateFaultTypes: any = ["POWEROFF_VM", "SUSPEND_VM", "RESET_VM"];

  public tagsData: any = {};
  public originalTagsData: any = {};

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "vmName": null
  };

  public searchedEndpoints: any = [];

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
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.fault = this.dataService.sharedData.fault;
    this.faultFormData.vmName = this.dataService.sharedData.vmName;
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

  public setEndpointVal(endpointVal) {
    this.faultFormData.endpointName = endpointVal;
  }

  public updateTags(tagsVal) {
    this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
  }

  public removeTag(tagKeyToRemove) {
    delete this.tagsData[tagKeyToRemove];
  }

  public displayEndpointFields(endpointNameVal) {
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        this.tagsData = this.commonUtils.getTagsData(this.originalTagsData,this.endpoints[i].tags);
      }
    }
  }

  public executeVcenterStateFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.faultService.executeVcenterStateFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        this.router.navigateByUrl('core/requests');
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
