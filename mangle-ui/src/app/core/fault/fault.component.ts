import { Component, OnInit } from "@angular/core";
import { DataService } from "src/app/shared/data.service";
import { CommonConstants } from "src/app/common/common.constants";

@Component({
  selector: "app-fault",
  templateUrl: "./fault.component.html"
})
export class FaultComponent implements OnInit {

  public infraExpand = false;
  public appExpand = false;
  public dbExpand = false;

  public dockerExpand = false;
  public vcenterExpand = false;
  public k8sExpand = false;
  public networkExpand = false;
  public awsExpand = false;
  public azureExpand = false;
  public redisExpand = false;

  constructor(private dataService: DataService) {
  }

  ngOnInit() {
    if (this.dataService.faultType === CommonConstants.INFRA_FAULTS) {
      this.infraExpand = true;
      if (this.dataService.infraSubType === CommonConstants.INFRA_FAULTS_AWS) {
        this.awsExpand = true;
      }
      if (this.dataService.infraSubType === CommonConstants.INFRA_FAULTS_AZURE) {
        this.azureExpand = true;
      }
      if (this.dataService.infraSubType === CommonConstants.INFRA_FAULTS_DOCKER) {
        this.dockerExpand = true;
      }
      if (this.dataService.infraSubType === CommonConstants.INFRA_FAULTS_VCENTER) {
        this.vcenterExpand = true;
      }
      if (this.dataService.infraSubType === CommonConstants.INFRA_FAULTS_K8S) {
        this.k8sExpand = true;
      }
      if (this.dataService.infraSubType === CommonConstants.INFRA_FAULTS_NETWORK) {
        this.networkExpand = true;
      }
    }
    if (this.dataService.faultType === CommonConstants.APP_FAULTS) {
      this.appExpand = true;
    }
    if (this.dataService.faultType === CommonConstants.DB_FAULTS) {
      this.dbExpand = true;
      if (this.dataService.infraSubType === CommonConstants.DB_FAULTS_REDIS) {
        this.redisExpand = true;
      }
    }
    this.dataService.faultType = null;
    this.dataService.infraSubType = null;
  }

}
