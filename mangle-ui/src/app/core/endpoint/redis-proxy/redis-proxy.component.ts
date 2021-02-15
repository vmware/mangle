import { Component, OnInit } from "@angular/core";
import { EndpointService } from "../endpoint.service";
import { CommonEndpoint } from "../common.endpoint";
import { CommonConstants } from "src/app/common/common.constants";

@Component({
  selector: "app-redis-proxy",
  templateUrl: "./redis-proxy.component.html"
})
export class RedisProxyComponent extends CommonEndpoint implements OnInit {
  constructor(endpointService: EndpointService) {
    super(endpointService, CommonConstants.REDIS_FI_PROXY);
  }

  ngOnInit() {
    this.getEndpoints();
  }
}
