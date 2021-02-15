import {Component, OnInit} from "@angular/core";
import {EndpointService} from "../endpoint.service";
import {CommonEndpoint} from "../common.endpoint";
import {CommonConstants} from "src/app/common/common.constants";

@Component({
  selector: "app-machine",
  templateUrl: "./machine.component.html"
})
export class MachineComponent extends CommonEndpoint implements OnInit {

  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.MACHINE);
  }

  ngOnInit() {
    this.getEndpoints();
    this.getCredentials();
  }

}
