import {Component, OnInit} from "@angular/core";
import {Router} from "@angular/router";
import {EndpointService} from "src/app/core/endpoint/endpoint.service";
import {FaultService} from "../../../fault.service";
import {DataService} from "src/app/shared/data.service";
import {CommonNetwork} from "../common.network";
import {CommonConstants} from "src/app/common/common.constants";
import {CommonUtils} from "src/app/shared/commonUtils";

@Component({
  selector: "app-packet-duplicate",
  templateUrl: "./../network.component.html"
})
export class PacketDuplicateComponent extends CommonNetwork implements OnInit {

  public percentageHidden = false;
  public latencyHidden = false;
  public faultDescription = "Execute Packet Duplicate Fault";

  constructor(faultService: FaultService, endpointService: EndpointService, router: Router,
              private dataService: DataService, commonUtils: CommonUtils) {
    super(endpointService, faultService, router, CommonConstants.PACKET_DUPLICATE_PERCENTAGE, commonUtils);
  }

  ngOnInit() {
    this.percentageHidden = true;
    this.getAllEndpoints();
    if (this.dataService.sharedData != null) {
      this.populateFaultData(this.dataService);
    }
  }


}
