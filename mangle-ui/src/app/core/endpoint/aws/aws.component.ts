import {Component, OnInit} from "@angular/core";
import {EndpointService} from "../endpoint.service";
import {CommonEndpoint} from "../common.endpoint";
import {CommonConstants} from "src/app/common/common.constants";

@Component({
  selector: "app-aws",
  templateUrl: "./aws.component.html"
})
export class AWSComponent extends CommonEndpoint implements OnInit {
  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.AWS);
  }

  ngOnInit() {
    this.getEndpoints();
    this.getCredentials();
  }

}
