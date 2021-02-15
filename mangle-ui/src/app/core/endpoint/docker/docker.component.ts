import {Component, OnInit} from "@angular/core";
import {EndpointService} from "../endpoint.service";
import {CommonEndpoint} from "../common.endpoint";
import {CommonConstants} from "src/app/common/common.constants";

@Component({
  selector: "app-docker",
  templateUrl: "./docker.component.html"
})
export class DockerComponent extends CommonEndpoint implements OnInit {
  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.DOCKER);
  }
  public dockerCertificates: boolean;


  ngOnInit() {
    this.getEndpoints();
    this.getCertificates();
  }

}
