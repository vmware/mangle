import { Component, OnInit } from '@angular/core';
import { CoreService } from './core.service';
import { AuthService } from '../auth/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-core',
  templateUrl: './core.component.html',
  styleUrls: ['./core.component.css']
})
export class CoreComponent implements OnInit {

  constructor(private coreService: CoreService, private authService: AuthService, private router: Router) { }

  public user: string;
  public domain: string;
  public isAdminUser: boolean;

  ngOnInit() {
    this.getMyDetails();
  }

  public getMyDetails() {
    this.coreService.getMyDetails().subscribe(
      res => {
        this.user = res.name;
        this.domain = res.name.split("@")[1];
        var roleString = "";
        for (var i = 0; i < res.roleNames.length; i++) {
          if (i > 0) {
            roleString = roleString + "&names=" + res.roleNames[i];
          } else {
            roleString = roleString + "names=" + res.roleNames[i];
          }
        }
        this.getMyRolesAndPrivileges(roleString);
      });
  }

  logout() {
    this.authService.logout();
    this.router.navigateByUrl('login');
  }

  public getMyRolesAndPrivileges(roleQueryString) {
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
