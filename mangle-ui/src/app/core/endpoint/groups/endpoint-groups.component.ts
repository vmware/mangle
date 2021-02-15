import { Component, EventEmitter, OnInit } from "@angular/core";
import { EndpointService } from "src/app/core/endpoint/endpoint.service";
import { ClrLoadingState } from "@clr/angular";
import { MessageConstants } from "src/app/common/message.constants";
import { CommonConstants } from "src/app/common/common.constants";
import { NgForm } from '@angular/forms';
import { CommonEndpoint } from '../common.endpoint';
@Component({
  selector: "app-endpoint-groups",
  templateUrl: "./endpoint-groups.component.html"
})
export class EndpointGroupsComponent extends CommonEndpoint implements OnInit {

  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.ENDPOINT_GROUP);
  }

  ngOnInit() {
    this.getEndpoints();
  }
}
