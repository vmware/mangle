import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { EndpointService } from 'src/app/core/endpoint/endpoint.service';
import { FaultService } from '../../../fault.service';
import { DataService } from 'src/app/shared/data.service';
import { CommonNetwork } from '../common.network';
import { CommonConstants } from 'src/app/common/common.constants';

@Component({
  selector: 'app-packet-duplicate',
  templateUrl: './../network.component.html'
})
export class PacketDuplicateComponent extends CommonNetwork implements OnInit {

  public alertMessage: string;
  public isErrorMessage: boolean;

  public latencyHidden: boolean = false;
  public percentageHidden: boolean = false;
  public faultDescription: string = "Execute Packet Duplicate Fault"


  constructor(faultService: FaultService, private endpointService: EndpointService, router: Router, private dataService: DataService) {
    super(faultService, router, CommonConstants.PACKET_DUPLICATE_PERCENTAGE);
  }

  ngOnInit() {
    this.percentageHidden = true;
    this.endpointService.getAllEndpoints().subscribe(
      res => {
        if (res.code) {
          this.endpoints = [];
        } else {
          this.endpoints = res;
        }
      }, err => {
        this.endpoints = [];
        this.isErrorMessage= true;
        this.alertMessage = err.error.description;
      });
    if (this.dataService.sharedData != null) {
      this.populateFaultData(this.dataService);
    }
  }


}