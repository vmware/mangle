import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { ServiceConstants } from './common/service.constants';

@Injectable()
export class PrivilegeGuardService implements CanActivate {

    constructor(private router: Router, private http: HttpClient) {
    }

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Promise<boolean> {
        return this.isAdminUser();
    }

    public async isAdminUser(): Promise<boolean> {
        try {
            let my_details_res: any = await this.http.get(ServiceConstants.USER_MANAGEMENT_USER).toPromise();
            var roleString = "";
            for (var i = 0; i < my_details_res.roleNames.length; i++) {
                if (i > 0) {
                    roleString = roleString + "&names=" + my_details_res.roleNames[i];
                } else {
                    roleString = roleString + "names=" + my_details_res.roleNames[i];
                }
            }
            let privileges_res: any = await this.http.get(ServiceConstants.ROLE_MANAGEMENT_ROLES + '?' + roleString).toPromise();
            var roleListData = privileges_res._embedded.roleList;
            var isUserAdmin: boolean = false;
            for (var i = 0; i < roleListData.length; i++) {
                for (var j = 0; j < roleListData[i].privilegeNames.length; j++) {
                    if (roleListData[i].privilegeNames[j] == 'ADMIN_READ_WRITE') {
                        isUserAdmin = true;
                        return true;
                    }
                }
            }
            if (!isUserAdmin) {
                this.router.navigateByUrl('');
                return false;
            }
        } catch (error) {
            return false;
        }
    }

}
