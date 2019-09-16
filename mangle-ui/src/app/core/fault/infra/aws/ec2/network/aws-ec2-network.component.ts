import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { CommonUtils } from 'src/app/shared/commonUtils';

@Component({
  selector: 'app-aws-ec2-network',
  templateUrl: './aws-ec2-network.component.html'
})
export class AwsEC2NetworkComponent implements OnInit {

  public errorAlertMessage: string;
  public successAlertMessage: string;

  public endpoints: any = [];
  public networkaultTypes: any = ["BLOCK_ALL_NETWORK_TRAFFIC"];

  public tagsData: any = {};
  public originalTagsData: any = {};
  public awsTagsData: any = {};

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public faultFormData: any = {
    "endpointName": null,
    "fault": null,
    "awsTags": {},
    "randomInjection": true
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
        this.errorAlertMessage = err.error.description;
      });
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

  public updateAwsTags(awsTagsVal) {
    this.awsTagsData[awsTagsVal.tagKey] = awsTagsVal.tagValue;
  }

  public removeAwsTag(awsTagKeyToRemove) {
    delete this.awsTagsData[awsTagKeyToRemove];
  }

  public displayEndpointFields(endpointNameVal) {
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        this.tagsData = this.commonUtils.getTagsData(this.originalTagsData,this.endpoints[i].tags);
      }
    }
  }

  public executeAwsEC2NetworkFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    if (this.awsTagsData != {}) {
      faultData.awsTags = this.awsTagsData;
    }
    this.faultService.executeAwsEC2NetworkFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        this.router.navigateByUrl('core/requests');
      }, err => {
        this.errorAlertMessage = err.error.description;
        if (this.errorAlertMessage === undefined) {
          this.errorAlertMessage = err.error.error;
        }
        this.runBtnState = ClrLoadingState.DEFAULT;
      });
  }

}
