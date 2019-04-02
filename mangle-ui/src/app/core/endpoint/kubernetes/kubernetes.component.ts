import { Component, OnInit } from '@angular/core';
import { EndpointService } from '../endpoint.service';
import { CommonEndpoint } from '../common.endpoint';

@Component({
  selector: 'app-kubernetes',
  templateUrl: './kubernetes.component.html',
  styleUrls: ['./kubernetes.component.css']
})
export class KubernetesComponent extends CommonEndpoint implements OnInit {
  
  constructor(endpointService: EndpointService) {
    super(endpointService, 'K8S_CLUSTER');
   }

  ngOnInit() {
	this.getEndpoints();
	this.getCredentials();
  }

}
