import {Component, OnInit} from "@angular/core";
import {EndpointService} from "../endpoint.service";
import {CommonEndpoint} from "../common.endpoint";
import {CommonConstants} from "src/app/common/common.constants";

@Component({
  selector: "app-kubernetes",
  templateUrl: "./kubernetes.component.html"
})
export class KubernetesComponent extends CommonEndpoint implements OnInit {

  public disabledResourceLabelsModal: boolean;
  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.K8S_CLUSTER);
  }

  ngOnInit() {
    this.getEndpoints();
    this.getCredentials();
  }

}
