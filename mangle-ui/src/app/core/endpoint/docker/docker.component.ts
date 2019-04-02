import { Component, OnInit } from '@angular/core';
import { EndpointService } from '../endpoint.service';
import { CommonEndpoint } from '../common.endpoint';

@Component({
  selector: 'app-docker',
  templateUrl: './docker.component.html',
  styleUrls: ['./docker.component.css']
})
export class DockerComponent extends CommonEndpoint implements OnInit {

  constructor(endpointService: EndpointService) {
    super(endpointService, 'DOCKER');
  }

  ngOnInit() {
    this.getEndpoints();
  }

}
