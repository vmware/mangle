import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ServiceConstants } from '../../common/service.constants';

@Injectable()
export class AuthGuardService implements CanActivate {

    constructor(private router: Router, private http: HttpClient) {
    }

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return this.http.get(ServiceConstants.USER_MANAGEMENT_USER).pipe(map(
            response => {
                if (response['error'] !== 'Unauthorized') {
                    return true;
                } else {
                    this.router.navigateByUrl('login');
                    return false;
                }
            }
        ));
    }

}
