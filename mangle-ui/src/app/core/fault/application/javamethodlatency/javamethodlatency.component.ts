import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { FaultService } from '../../fault.service';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { ClrLoadingState } from '@clr/angular';
import { DataService } from 'src/app/shared/data.service';
import { CommonConstants } from 'src/app/common/common.constants';
import { CommonUtils } from 'src/app/shared/commonUtils';

@Component({
  selector: 'app-javamethodlatency',
  templateUrl: './javamethodlatency.component.html'
})
export class JavaMethodLatencyComponent implements OnInit {

  public errorAlertMessage: string;
  public successAlertMessage: string;

  public cronModal: boolean = false;

  public disableRun: boolean = false;

  public tagsData: any = {};
  public originalTagsData: any = {};

  public timeInMillisecondsHidden: boolean = true;
  public cronExpressionHidden: boolean = true;
  public descriptionHidden: boolean = true;

  public endpoints: any = [];
  public dockerHidden: boolean = true;
  public k8sHidden: boolean = true;

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public dockerContainers: any = [];

  public faultFormData: any = {
    "injectionHomeDir": null,
    "className": null,
    "latency": null,
    "methodName": null,
    "ruleEvent": null,

    "dockerArguments": {
      "containerName": null
    },
    "endpointName": null,
    "jvmProperties": {
      "javaHomePath": null,
      "jvmprocess": null,
      "port": 9091,
      "user": null
    },
    "k8sArguments": {
      "containerName": null,
      "enableRandomInjection": true,
      "podLabels": null
    }
  };

  public searchedEndpoints: any = [];
  public searchedContainers: any = [];

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
    this.faultFormData.injectionHomeDir = this.dataService.sharedData.injectionHomeDir;
    this.faultFormData.endpointName = this.dataService.sharedData.endpointName;
    this.faultFormData.latency = this.dataService.sharedData.latency;
    this.faultFormData.className = this.dataService.sharedData.className;
    this.faultFormData.methodName = this.dataService.sharedData.methodName;
    this.faultFormData.ruleEvent = this.dataService.sharedData.ruleEvent;
    this.faultFormData.jvmProperties = this.dataService.sharedData.jvmProperties;

    if (this.dataService.sharedData.dockerArguments != null) {
      this.faultFormData.dockerArguments = this.dataService.sharedData.dockerArguments;
      this.dockerHidden = false;
    }
    if (this.dataService.sharedData.k8sArguments != null) {
      this.faultFormData.k8sArguments = this.dataService.sharedData.k8sArguments;
      this.k8sHidden = false;
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

  public searchContainer(searchKeyWord) {
    this.searchedContainers = [];
    for (var i = 0; i < this.dockerContainers.length; i++) {
      if (this.dockerContainers[i].indexOf(searchKeyWord) > -1) {
        this.searchedContainers.push(this.dockerContainers[i]);
      }
    }
  }

  public setEndpointVal(endpointVal) {
    this.faultFormData.endpointName = endpointVal;
  }

  public setContainerVal(containerVal) {
    this.faultFormData.dockerArguments.containerName = containerVal;
  }

  public getDockerContainers(epType, epName) {
    if (epType == "DOCKER") {
      this.endpointService.getDockerContainers(epName).subscribe(
        res => {
          if (res.code) {
            this.dockerContainers = [];
          } else {
            this.dockerContainers = res;
          }
        }, err => {
          this.dockerContainers = [];
          this.errorAlertMessage = err.error.description;
        }
      );
    }
  }

  public updateTags(tagsVal) {
    this.tagsData[tagsVal.tagKey] = tagsVal.tagValue;
  }

  public removeTag(tagKeyToRemove) {
    delete this.tagsData[tagKeyToRemove];
  }

  public displayEndpointFields(endpointNameVal){
    this.dockerHidden = true;
    this.k8sHidden = true;
    this.tagsData = {};
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) 
      { 
        this.tagsData = this.commonUtils.getTagsData(this.originalTagsData,this.endpoints[i].tags);
        if (this.endpoints[i].endPointType == 'DOCKER') {
          this.dockerHidden = false;
        }
        if (this.endpoints[i].endPointType == 'K8S_CLUSTER') {
          this.k8sHidden = false;
        }
      }
    }
  }

  public setSubmitButton() {
    this.disableRun = false;
  }

  public executeJavaMethodLatencyFault(faultData) {
    this.runBtnState = ClrLoadingState.LOADING;
    if (this.tagsData != {}) {
      faultData.tags = this.tagsData;
    }
    this.faultService.executeJavaMethodLatencyFault(faultData).subscribe(
      res => {
        this.tagsData = {};
        this.router.navigateByUrl(CommonConstants.REQUESTS_PROCESSED_URL);
      }, err => {
        this.errorAlertMessage = err.error.description;
        if (this.errorAlertMessage === undefined) {
          this.errorAlertMessage = err.error.error;
        }
        this.runBtnState = ClrLoadingState.DEFAULT;
      });
  }

}
