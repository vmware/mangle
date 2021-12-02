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

  	public k8sResources : any = [];
  	public searchedResources: any;
  	public resourceListPopulated : any = null;

    public dockerHidden = true;
    public k8sHidden = true;

    public disableSchedule = true;
    public disableRun = false;

    public runBtnState: ClrLoadingState = ClrLoadingState.DEFAULT;
    public notifiersData: any = [];
    public tagsModal: boolean;
    public notifierModal: boolean;
    public epDbType: any;
    public nodes: any = [];
    public pods: any = [];
    nodeListPopulated: any = null;
    searchedNodes: any[];

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

    public displayNodeFields(nodeNameVal: any) {
        this.dockerHidden = true;
        this.k8sHidden = true;
        this.tagsData = {};
        for (var i = 0; i < this.nodes.length; i++) {
            if (nodeNameVal == this.nodes[i].name) {
                this.tagsData = this.commonUtils.getTagsData(this.originalTagsData, this.nodes[i].tags);

                if (!this.nodes[i].isEmpty()) {
                    this.k8sHidden = false;
                }
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
    public getAllNodes(epName) {
        console.log(epName);
        this.nodeListPopulated = 'loading';
        this.endpointService.getAllNodes(epName).subscribe(
            res => {
                if (res.code) {
                    this.nodes = [];
                } else {
                    this.nodes = res.content;
                    console.log(this.nodes);
                }if (this.nodes.length == 0){
          this.nodeListPopulated = 'noReadyNodeFound';
        } else{
          this.nodeListPopulated = 'ReadyNodeFound';
        }
      },err => {
                this.nodes = [];
                this.isErrorMessage = true;
                this.nodeListPopulated = null;
                this.alertMessage = err.error.description;
      }
    );
  }

    public searchNodes(searchKeyWord) {
        this.searchedNodes = [];
        for (var i = 0; i < this.nodes.length; i++) {
            if (this.nodes[i].indexOf(searchKeyWord) > -1) {
                this.searchedNodes.push(this.nodes[i]);
            }
        }
    }

  public getAllResources(epName: any,resourceType: any){
    this.resourceListPopulated = 'loading';
    this.endpointService.getK8sResources(epName,resourceType).subscribe(
      res => {
        if (res.code) {
          this.k8sResources = [];
        } else {
          this.k8sResources = res.content;
        }
        if (this.k8sResources.length == 0){
          this.resourceListPopulated = 'noResourceFound';
        } else{
          this.resourceListPopulated = null;
        }
      }, err => {
        this.k8sResources = [];
        this.isErrorMessage = true;
        this.resourceListPopulated = null;
        this.alertMessage = err.error.description;
      }
    );
  }

  public searchResource(searchKeyWord) {
    this.searchedResources = [];
    for (var i = 0; i < this.k8sResources.length; i++) {
      if (this.k8sResources[i].indexOf(searchKeyWord) > -1) {
        this.searchedResources.push(this.k8sResources[i]);
      }
    }
}

}
