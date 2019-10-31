import { Component, OnInit, Inject } from '@angular/core';
import { Router } from '@angular/router';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { FaultService } from '../../../fault.service';
import { MessageConstants } from 'src/app/common/message.constants';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { DOCUMENT } from '@angular/common';
import { CommonUtils } from 'src/app/shared/commonUtils';

@Component({
  selector: 'app-delete-k8s-resource',
  templateUrl: './delete-k8s-resource.component.html'
})
export class DeleteK8SResourceComponent implements OnInit {

  public alertMessage: string;
  public isErrorMessage: boolean;

  public resourceNameHidden: boolean = true;
  public resourceLabelsHidden: boolean = true;
  public resourceLabelsData: any = {};

  public tagsData: any = {};
  public originalTagsData: any = {};

  public endpoints: any = [];
  public k8sResourceTypes: any = ["POD", "NODE", "SERVICE", "DEPLOYMENT", "STATEFULSET", "SECRET", "DAEMONSET", "CONFIGMAP", "JOB", "REPLICASET", "REPLICATIONCONTROLLER", "PV", "PVC"];

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public faultFormData: any = {
    "endpointName": null,
    "resourceType": null,
    "resourceName": null,
    "resourceLabels": {},
    "randomInjection": false,
    "injectionHomeDir": null
  };

  public searchedEndpoints: any = [];

  constructor(private faultService: FaultService, private endpointService: EndpointService, private router: Router, private dataService: DataService, @Inject(DOCUMENT) document, private commonUtils: CommonUtils) {

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
    this.faultFormData.resourceType = this.dataService.sharedData.resourceType;
    this.faultFormData.resourceName = this.dataService.sharedData.resourceName;
    this.faultFormData.resourceLabels = this.dataService.sharedData.resourceLabels;
    this.resourceLabelsData = this.dataService.sharedData.resourceLabels;
    this.faultFormData.randomInjection = this.dataService.sharedData.randomInjection;
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    if (this.dataService.sharedData.resourceName != null) {
      document.getElementById("resourceName").click();
    } else {
      document.getElementById("resourceLabels").click();
    }
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

  public setResourceVal(selectedResource) {
    this.resourceNameHidden = true;
    this.resourceLabelsHidden = true;
    if (selectedResource == "resourceName") {
      this.resourceNameHidden = false;
    }
    if (selectedResource == "resourceLabels") {
      this.resourceLabelsHidden = false;
    }
  }

  public updateResourceLabels(resourceLabelsVal) {
    this.resourceLabelsData[resourceLabelsVal.resourceLabelsKey] = resourceLabelsVal.resourceLabelsValue;
  }

  public removeResourceLabels(resourceLabelsKeyToRemove) {
    delete this.resourceLabelsData[resourceLabelsKeyToRemove];
  }

  public executeK8SDeleteResourceFault(faultData) {
    if (!this.resourceNameHidden) {
      delete faultData["resourceLabels"];
      faultData.randomInjection = true;
    } else {
      faultData.resourceLabels = this.resourceLabelsData;
      if (JSON.stringify(faultData.resourceLabels) === JSON.stringify({})) {
        this.isErrorMessage= true;
        this.alertMessage = MessageConstants.RESOURCE_LABEL_REQUIRED;
        return false;
      }
      delete faultData["resourceName"];
    }
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.runBtnState = ClrLoadingState.LOADING;
    this.faultService.executeK8SDeleteResourceFault(faultData).subscribe(
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
