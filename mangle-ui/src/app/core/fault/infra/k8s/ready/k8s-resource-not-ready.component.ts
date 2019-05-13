import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { FaultService } from '../../../fault.service';
import { MessageConstants } from 'src/app/common/message.constants';
import { ClrLoadingState } from '@clr/angular';

@Component({
  selector: 'app-k8s-resource-not-ready',
  templateUrl: './k8s-resource-not-ready.component.html',
  styleUrls: ['./k8s-resource-not-ready.component.css']
})
export class K8SResourceNotReadyComponent implements OnInit {

  public errorFlag = false;
  public successFlag = false;
  public alertMessage: string;

  public resourceNameHidden: boolean = true;
  public resourceLabelsHidden: boolean = true;
  public resourceLabelsData: any = {};

  public tagsData: any = {};

  public endpoints: any = [];
  public k8sResourceTypes: any = ["POD", "NODE"];
  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;

  public faultFormData: any = {
    "endpointName": null,
    "resourceName": null,
    "resourceLabels": {},
    "appContainerName": null,
    "injectionHomeDir": null,
    "randomInjection": true
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

  public executeK8SResourceNotReadyFault(faultData) {
    this.errorFlag = false;
    this.successFlag = false;
    if (!this.resourceNameHidden) {
      delete faultData["resourceLabels"];
      faultData.randomInjection = true;
    } else {
      faultData.resourceLabels = this.resourceLabelsData;
      if (JSON.stringify(faultData.resourceLabels) === JSON.stringify({})) {
        this.alertMessage = MessageConstants.RESOURCE_LABEL_REQUIRED;
        this.errorFlag = true;
        return false;
      }
      delete faultData["resourceName"];
    }
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.runBtnState = ClrLoadingState.LOADING;
    this.faultService.executeK8SResourceNotReadyFault(faultData).subscribe(
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
