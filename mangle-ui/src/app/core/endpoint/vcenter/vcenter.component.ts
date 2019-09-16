import { Component, OnInit } from '@angular/core';
import { EndpointService } from '../endpoint.service';
import { CommonEndpoint } from '../common.endpoint';

@Component({
  selector: 'app-vcenter',
  templateUrl: './vcenter.component.html'
})
export class VcenterComponent extends CommonEndpoint implements OnInit {
  
  constructor(endpointService: EndpointService) { 
    super(endpointService, 'VCENTER');
  }

  ngOnInit() {
	  this.getEndpoints();
	  this.getCredentials();
  }

}
