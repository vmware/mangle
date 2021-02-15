import { Component, OnInit } from '@angular/core';
import { CoreComponent } from '../core.component';

@Component({
  selector: 'app-requests',
  templateUrl: './requests.component.html'
})
export class RequestsComponent implements OnInit {

  public isAdminUser: boolean;

  constructor(private coreComponent: CoreComponent) { }

  ngOnInit() {
    this.isAdminUser = this.coreComponent.isAdminUser;
   }

}
