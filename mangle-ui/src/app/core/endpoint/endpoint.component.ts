import { Component, OnInit } from "@angular/core";
import { CoreComponent } from '../core.component';

@Component({
  selector: "app-endpoint",
  templateUrl: "./endpoint.component.html"
})
export class EndpointComponent implements OnInit {

  public isAdminUser: boolean;

  constructor(private coreComponent: CoreComponent) {
  }

  ngOnInit() {
    this.isAdminUser = this.coreComponent.isAdminUser;
  }

}
