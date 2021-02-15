import { EndpointService } from "../endpoint/endpoint.service";
import { CommonConstants } from "src/app/common/common.constants";
import { CommonUtils } from "src/app/shared/commonUtils";
import { OnInit } from "@angular/core";
import { ClrLoadingState } from "@clr/angular";
import { DataService } from "src/app/shared/data.service";

export class FaultCommons implements OnInit {
  public cronModal = false;

  public tagsData: any = {};
  public originalTagsData: any = {};

  public alertMessage: string;
  public isErrorMessage: boolean;

  public endpoints: any = [];

  public dockerContainers: any = [];
  public searchedContainers: any = [];

  public dockerHidden = true;
  public k8sHidden = true;

  public disableSchedule = true;
  public disableRun = false;

  public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
  public notifiersData: any = [];
  public tagsModal: boolean;
  public notifierModal: boolean;
  public epDbType: any;

  ngOnInit() {
  }

  constructor(private endpointService: EndpointService, private commonUtils: CommonUtils) {
  }

  public getAllEndpoints() {
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        if (res.code) {
          this.endpoints = [];
        } else {
          this.endpoints = res.content;
        }
      }, err => {
        this.endpoints = [];
        this.isErrorMessage = true;
        this.alertMessage = err.error.description;
      });
  }

  public searchContainer(searchKeyWord: any) {
    this.searchedContainers = [];
    for (var i = 0; i < this.dockerContainers.length; i++) {
      if (this.dockerContainers[i].indexOf(searchKeyWord) > -1) {
        this.searchedContainers.push(this.dockerContainers[i]);
      }
    }
  }

  public getDockerContainers(epType: any, epName: any) {
    if (epType == CommonConstants.DATABASE) {
      const tempEndpoint = this.searchParentEndpointName(epName);
      if (tempEndpoint != null) {
        epType = tempEndpoint.databaseConnectionProperties.parentEndpointType;
        epName = tempEndpoint.databaseConnectionProperties.parentEndpointName;
      }
    }
    if (epType == CommonConstants.DOCKER) {
      this.endpointService.getDockerContainers(epName).subscribe(
        res => {
          if (res.code) {
            this.dockerContainers = [];
          } else {
            this.dockerContainers = res.content;
          }
        }, err => {
          this.dockerContainers = [];
          this.isErrorMessage = true;
          this.alertMessage = err.error.description;
        }
      );
    }
  }

  public viewCronModal(modalVal) {
    this.cronModal = modalVal;
  }

  public displayEndpointFields(endpointNameVal: any) {
    this.dockerHidden = true;
    this.k8sHidden = true;
    this.tagsData = {};
    for (var i = 0; i < this.endpoints.length; i++) {
      if (endpointNameVal == this.endpoints[i].name) {
        this.tagsData = this.commonUtils.getTagsData(this.originalTagsData, this.endpoints[i].tags);
        if (this.endpoints[i].endPointType == CommonConstants.DOCKER) {
          this.dockerHidden = false;
        }
        if (this.endpoints[i].endPointType == CommonConstants.K8S_CLUSTER) {
          this.k8sHidden = false;
        }
        if (this.endpoints[i].endPointType == CommonConstants.DATABASE) {
          this.validateParentEndpointType(this.endpoints[i])
        }
        break;
      }
    }
  }

  public populateFaultNotifiers(dataService: DataService) {
    if (dataService.sharedData.notifierNames != null && dataService.sharedData.notifierNames.length > 0) {
      this.notifiersData = dataService.sharedData.notifierNames;
    }
  }

  public addNotifiersInFault(faultData: any) {
    if (this.notifiersData !== [] && this.notifiersData.length > 0) {
      faultData.notifierNames = this.notifiersData;
    }
  }

  public validateParentEndpointType(endpoint: any) {
    this.epDbType = endpoint.databaseConnectionProperties.dbType;
    if (endpoint.databaseConnectionProperties.parentEndpointType == CommonConstants.DOCKER) {
      this.dockerHidden = false;
    }
    if (endpoint.databaseConnectionProperties.parentEndpointType == CommonConstants.K8S_CLUSTER) {
      this.k8sHidden = false;
    }
  }

  public searchParentEndpointName(epName: string): any {
    let tempEP = null;
    for (var i = 0; i < this.endpoints.length; i++) {
      const currEndpoint = this.endpoints[i];
      if (epName == currEndpoint.name) {
        tempEP = currEndpoint
        break;
      }
    }
    return tempEP;
  }
}
