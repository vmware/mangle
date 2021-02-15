import { Component, OnInit } from "@angular/core";
import { EndpointService } from "../endpoint.service";
import { CommonEndpoint } from "../common.endpoint";
import { CommonConstants } from "src/app/common/common.constants";
import { Router } from "@angular/router";

@Component({
  selector: "app-database",
  templateUrl: "./database.component.html"
})
export class DatabaseComponent extends CommonEndpoint implements OnInit {
  constructor(endpointService: EndpointService, private router: Router, ) {
    super(endpointService, CommonConstants.DATABASE);
  }

  public searchedDbEndpoints: any = [];
  public supportedEpTypes: any = [CommonConstants.MACHINE, CommonConstants.K8S_CLUSTER, CommonConstants.DOCKER];
  public viewParentEndpoint = false;
  public parentEndpoint = {};

  ngOnInit() {
    this.getEndpoints();
    this.getAllEndpoints()
    this.getCredentials();
  }

  public searchDbEndpoint(searchKeyWord: any) {
    this.searchedDbEndpoints = [];
    for (var i = 0; i < this.allEndpoints.length; i++) {
      if (this.allEndpoints[i].name.indexOf(searchKeyWord) > -1) {
        this.searchedDbEndpoints.push(this.allEndpoints[i]);
      }
    }
  }

  public setParentEndpointNameVal(epNameVal: any) {
    this.epFormData.databaseConnectionProperties.parentEndpointName = epNameVal;
    this.updateParentEndpointTags(epNameVal);
  }

  public getParentEndpointByName(endpoint: any) {
    this.parentEndpoint = {};
    const parentEndpointName = endpoint.databaseConnectionProperties.parentEndpointName;
    for (var i = 0; i < this.allEndpoints.length; i++) {
      if (this.allEndpoints[i].name === parentEndpointName) {
        this.parentEndpoint = this.allEndpoints[i];
        break;
      }
    }
  }

  public updateParentEndpointTags(epNameVal: any) {
    for (var i = 0; i < this.allEndpoints.length; i++) {
      const tempEP = this.allEndpoints[i];
      if (tempEP.name === epNameVal) {
        if (tempEP.tags != null) {
          this.tagsData = tempEP.tags;
        } else {
          this.tagsData = {};
        }
        break;
      }
    }
  }
}
