import {Component, OnInit} from "@angular/core";
import {EndpointService} from "../endpoint.service";
import {CommonEndpoint} from "../common.endpoint";
import {CommonConstants} from "src/app/common/common.constants";

@Component({
  selector: "app-azure",
  templateUrl: "./azure.component.html"
})
export class AzureComponent extends CommonEndpoint implements OnInit {
  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.AZURE);
  }

  ngOnInit() {
    this.getEndpoints();
    this.getCredentials();
  }

}
