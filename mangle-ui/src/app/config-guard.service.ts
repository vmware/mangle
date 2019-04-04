import { Injectable, NgZone } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { map } from 'rxjs/operators';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable()
export class ConfigGuardService implements CanActivate {

    constructor(private router: Router, private http: HttpClient, private ngZone: NgZone) {
    }

    public canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): Observable<boolean> {
        return this.http.get('/mangle-services/rest/api/v1/authentication-management/users/admin').pipe(map(
            response => {
                if (response['content']) {
                    return true;
                } else {
                    this.ngZone.run(() => this.router.navigateByUrl('config')).then();
                    //this.router.navigateByUrl('config');
                    return false;
                }
            }
        ));
    }

}
