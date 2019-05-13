import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';

@Component({
  selector: 'app-vcenter-nic',
  templateUrl: './vcenter-nic.component.html',
  styleUrls: ['./vcenter-nic.component.css']
})
export class VcenterNicComponent implements OnInit {

  public errorFlag = false;
  public successFlag = false;
  public alertMessage: string;

  public endpoints: any = [];
  public nicFaultTypes: any = ["DISCONNECT_NIC"];

  public tagsData: any = {};

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "vmNicId": null,
    "vmName": null
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

  public displayEndpointFields(endpointNameVal) {
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        if (this.endpoints[i].tags != null) {
          this.tagsData = this.endpoints[i].tags;
        } else {
          this.tagsData = {};
        }
      }
    }
  }

  public executeVcenterNicFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    this.errorFlag = false;
    this.successFlag = false;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.faultService.executeVcenterNicFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        this.router.navigateByUrl('core/requests');
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
