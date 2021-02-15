import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { FaultService } from '../../../fault.service';
import { DataService } from 'src/app/shared/data.service';
import { CommonNetwork } from '../common.network';
import { CommonConstants } from 'src/app/common/common.constants';
import { CommonUtils } from 'src/app/shared/commonUtils';

@Component({
  selector: 'app-network-delay',
  templateUrl: './../network.component.html'
})
export class PacketDelayComponent extends CommonNetwork implements OnInit {

  public latencyHidden: boolean = false;
  public percentageHidden: boolean = false;
  public faultDescription: string = "Execute Packet Delay Fault"

  constructor(faultService: FaultService, endpointService: EndpointService, router: Router, private dataService: DataService, commonUtils: CommonUtils) {
    super(endpointService, faultService, router, CommonConstants.NETWORK_DELAY_MILLISECONDS, commonUtils);
  }

  ngOnInit() {
    this.latencyHidden = true;
    this.getAllEndpoints();
    if (this.dataService.sharedData != null) {
      this.populateFaultData(this.dataService);
    }
  }

}