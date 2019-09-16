import { Component, OnInit } from '@angular/core';
import { CoreService } from '../core.service';

@Component({
  selector: 'app-requests',
  templateUrl: './requests.component.html'
})
export class RequestsComponent implements OnInit {

  constructor(private coreService: CoreService) { }

  public isAdminUser: boolean;

  ngOnInit() {
    this.getUserDetails();
  }

  public getUserDetails() {
    this.coreService.getMyDetails().subscribe(
      res => {
        var roleString = "";
        for (var i = 0; i < res.roleNames.length; i++) {
          if (i > 0) {
            roleString = roleString + "&names=" + res.roleNames[i];
          } else {
            roleString = roleString + "names=" + res.roleNames[i];
          }
        }
        this.getUserRolesAndPrivileges(roleString);
      });
  }

  public getUserRolesAndPrivileges(roleQueryString) {
    this.coreService.getMyRolesAndPrivileges(roleQueryString).subscribe(
      res => {
        var roleListData = res._embedded.roleList;
        for (var i = 0; i < roleListData.length; i++) {
          for (var j = 0; j < roleListData[i].privilegeNames.length; j++) {
            if (roleListData[i].privilegeNames[j] == 'ADMIN_READ_WRITE') {
              this.isAdminUser = true;
            }
          }
        }
      }
    );
  }
}
